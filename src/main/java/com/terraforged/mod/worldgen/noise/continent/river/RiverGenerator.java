/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.worldgen.noise.continent.river;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentGenerator;
import com.terraforged.mod.worldgen.noise.continent.cell.CellPoint;
import com.terraforged.mod.worldgen.noise.continent.config.ContinentConfig;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2i;

public class RiverGenerator {
    public static final Vec2i[] DIRS = {new Vec2i(1, 0), new Vec2i(0, 1), new Vec2i(-1, 0), new Vec2i(0, -1)};

    private static final int X_OFFSET = 8657124;
    private static final int Y_OFFSET = 5123678;

    private static final int RIVER_CACHE_SIZE = 1024;
    private static final int RIVER_POOL_SIZE = RIVER_CACHE_SIZE + 1;

    private final int seed;
    private final float lakeDensity;
    private final ContinentGenerator continent;
    private final RiverCarver riverCarver;
    private final Domain riverWarp;
    private final ThreadLocal<CarverSample> localRiverSample = ThreadLocal.withInitial(CarverSample::new);

    private final ObjectPool<RiverPieces> pool = new ObjectPool<>(RIVER_POOL_SIZE, RiverPieces::new);
    private final LossyCache<RiverPieces> cache = new LossyCache.Concurrent<>(RIVER_CACHE_SIZE, RiverPieces[]::new, pool::restore);

    public RiverGenerator(ContinentGenerator continent, ContinentConfig config) {
        this.continent = continent;
        this.seed = config.rivers.seed;
        this.lakeDensity = config.rivers.lakeDensity;
        this.riverCarver = new RiverCarver(continent.levels, config);
        this.riverWarp = Domain.warp(
                Source.builder().seed(seed + X_OFFSET).frequency(30).simplex(),
                Source.builder().seed(seed + Y_OFFSET).frequency(30).simplex(),
                Source.constant(0.004)
        );
    }

    public void sample(float x, float y, NoiseSample sample) {
        float px = riverWarp.getX(x, y);
        float py = riverWarp.getY(x, y);

        var nodeSample = localRiverSample.get().reset();

        sample(px, py, nodeSample);

        riverCarver.carve(px, py, sample, nodeSample);
    }

    private void sample(float x, float y, CarverSample sample) {
        var centre = continent.getNearestCell(x, y);
        int centreX = PosUtil.unpackLeft(centre);
        int centreY = PosUtil.unpackRight(centre);

        // Note: Must adjust inputs AFTER getting nearest cell
        x = continent.cellShape.adjustX(x);
        y = continent.cellShape.adjustY(y);

        int minX = centreX - 1;
        int minY = centreY - 1;
        int maxX = centreX + 1;
        int maxY = centreY + 1;

        RiverNode river = null;
        RiverNode lake = null;

        for (int cy = minY; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++) {
                var pieces = getNodes(cx, cy);

                for (int i = 0; i < pieces.riverCount(); i++) {
                    var node = pieces.river(i);
                    river = sampleNode(x, y, node, river, sample.river);
                }

                for (int i = 0; i < pieces.lakeCount(); i++) {
                    var node = pieces.lake(i);
                    lake = sampleNode(x, y, node, lake, sample.lake);
                }
            }
        }

        recordNode(river, sample.river);
        recordNode(lake, sample.lake);
    }

    private RiverNode sampleNode(float x, float y, RiverNode node, RiverNode nearest, NodeSample sample) {
        float t = node.getProjection(x, y);
        float d = node.getDistance2(x, y, t);

        if (d < sample.distance) {
            nearest = node;
            sample.distance = d;
            sample.projection = t;
        }

        return nearest;
    }

    private void recordNode(RiverNode node, NodeSample sample) {
        if (node != null) {
            float level = node.getHeight(sample.projection);
            float radius = node.getRadius(sample.projection);
            sample.distance = NoiseUtil.sqrt(sample.distance);
            sample.position = radius;
            sample.level = continent.shapeGenerator.getBaseNoise(level);
        } else {
            sample.invalidate();
        }
    }

    private RiverPieces getNodes(int x, int y) {
        long index = PosUtil.pack(x, y);
        return cache.computeIfAbsent(index, this::computeNodes);
    }

    private RiverPieces computeNodes(long index) {
        int ax = PosUtil.unpackLeft(index);
        int ay = PosUtil.unpackRight(index);

        var a = continent.getCell(ax, ay);
        if (continent.shapeGenerator.getThresholdValue(a) <= 0) return RiverPieces.NONE;

        var min = a;
        float minValue = getBaseValue(a);

        float ah = getHeight(a.noise(), 0, 1);
        float ar = getRadius(a.noise(), 0, 1);

        boolean isSource = true;
        var pieces = pool.take();
        for (var dir : DIRS) {
            int bx = ax + dir.x;
            int by = ay + dir.y;
            var b = continent.getCell(bx, by);

            float value = getBaseValue(b);

            // Track the lowest neighbour as the candidate to connect A to
            if (value <= minValue) {
                min = b;
                minValue = value;
                continue;
            }

            if (value <= 0) continue;

            // Check if B is higher and A is its lowest neighbour
            if (connects(ax, ay, bx, by, value)) {
                float bh = getHeight(b.noise(), 0, 1);
                float br = getRadius(b.noise(), 0, 1);
                int hash = MathUtil.hash(seed + 827614, bx, by);

                addRiverNodes(a, b, ah, bh, ar, br, hash, pieces);

                isSource = false;
            }
        }

        // No lower-neighbour to connect to
        if (min == a) {
            return pieces;
        }

        // Cull tiny chode-rivers
        if (isSource && pieces.riverCount() == 0) {
            pool.restore(pieces);
            return RiverPieces.NONE;
        }

        float bh = getHeight(min.noise(), 0, 1);
        float br = getRadius(min.noise(), 0, 1);
        int hash = MathUtil.hash(seed + 827614, ax, ay);

        addRiverNodes(a, min, ah, bh, ar, br, hash, pieces);

        return pieces;
    }

    private void addRiverNodes(CellPoint a, CellPoint b, float ah, float bh, float ar, float br, int hash, RiverPieces pieces) {
        // Mid-point on border between cell points A & B
        float mx = (a.px + b.px) * 0.5f;
        float my = (a.py + b.py) * 0.5f;
        float mr = (ar + br) * 0.5f;
        float mh = (ah + bh) * 0.5f;

        // Mid-point between A & M
        float cx = (a.px + mx) * 0.5f;
        float cy = (a.py + my) * 0.5f;
        float cr = (ar + mr) * 0.5f;
        float ch = (ah + mh) * 0.5f;

        // Normals to AC
        float nx = -(cy - a.py);
        float ny = (cx - a.px);

        float dir0 = MathUtil.rand(seed + 2112413, hash) < 0.5f ? -1f : 1f;
        float dir1 = MathUtil.rand(seed + 9873542, hash) < 0.5f ? -1f : 1f;
        float amp0 = 0.5f + MathUtil.rand(seed + 8794, hash) * 0.5f;
        float amp1 = 0.5f + MathUtil.rand(seed + 4561, hash) * 0.5f;

        // Displace point C perpendicularly to AC
        float displacement = 0.4f * amp0;
        cx += nx * displacement;
        cy += ny * displacement;

        float warpStrength = 0.3f * dir1 * amp1;
        float warp1 = warpStrength * NoiseUtil.map(a.noise, 0.4f, 0.6f, 0.2f);
        float warp2 = -warpStrength * NoiseUtil.map(b.noise, 0.4f, 0.6f, 0.2f);

        pieces.addRiver(new RiverNode(a.px, a.py, cx, cy, ah, ch, ar, cr, warp1));
        pieces.addRiver(new RiverNode(cx, cy, mx, my, ch, mh, cr, mr, warp2));

        // If B is an ocean cell then extend the connection all the way to prevent
        // rivers stopping short at the coast/beach. Note: we must use the 'b.noise'
        // here and not the low-octave 'b.noise()'.
        if (b.noise < continent.shapeGenerator.threshold) {
            pieces.addRiver(new RiverNode(mx, my, b.px, b.py, mh, bh, mr, br, warp1));
        }
    }

    private void addLakeNodes(CellPoint a, CellPoint b, float ah, float ar, RiverPieces pieces) {
        float dx = a.px - b.px;
        float dy = a.py - b.py;
        float cx = a.px + dx * 0.001f;
        float cy = a.py + dy * 0.001f;
        pieces.addLake(new RiverNode(a.px, a.py, cx, cy, ah, ah, 1, 1, 0));
    }

    private boolean connects(int ax, int ay, int bx, int by, float minValue) {
        int minY = bx;
        int minX = by;

        for (var dir : DIRS) {
            int cx = bx + dir.x;
            int cy = by + dir.y;
            var c = continent.getCell(cx, cy);
            float value = getBaseValue(c);

            if (value < minValue) {
                minX = cx;
                minY = cy;
                minValue = value;
            }
        }

        return minX == ax && minY == ay;
    }

    private float getBaseValue(CellPoint point) {
        return continent.shapeGenerator.getThresholdValue(point) <= 0 ? 0 : point.noise();
    }

    private float getHeight(float noise, float min, float max) {
        return noise;
    }

    private float getRadius(float noise, float min, float max) {
        float lower = 0.5f;
        float upper = 0.7f;

        noise = NoiseUtil.map(noise, lower, upper, (upper - lower));
        noise = 1 - noise;

        return noise;
    }
}
