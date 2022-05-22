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
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentConfig;
import com.terraforged.mod.worldgen.noise.continent.ContinentGenerator;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;
import com.terraforged.mod.worldgen.noise.continent.cell.CellPoint;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2i;

import java.util.ArrayList;
import java.util.List;

public class RiverGenerator {
    protected static final Vec2i[] DIRS = {new Vec2i(1, 0), new Vec2i(0, 1), new Vec2i(-1, 0), new Vec2i(0, -1)};
    protected static final int RIVER_CACHE_SIZE = 1024;

    private final ContinentGenerator continent;
    private final ThreadLocal<RiverSample> localRiverSample = ThreadLocal.withInitial(RiverSample::new);
    private final ThreadLocal<List<RiverNode>> localBuilder = ThreadLocal.withInitial(ArrayList::new);
    private final LossyCache<RiverNode[]> cache = new LossyCache.Concurrent<>(RIVER_CACHE_SIZE, RiverNode[][]::new, t -> {});

    private final float minRadius;
    private final float maxRadius;
    private final float blendRadius;

    public RiverGenerator(ContinentGenerator continent, ContinentConfig config) {
        this.continent = continent;
        this.minRadius = config.rivers.riverMinRadius;
        this.maxRadius = config.rivers.riverMaxRadius;
        this.blendRadius = Math.min(1.0f, maxRadius + 0.2f);
    }

    public float getValue(float x, float y) {
        x = continent.cellShape.adjustX(x);
        y = continent.cellShape.adjustY(y);

        int maxX = NoiseUtil.floor(x) + 1;
        int maxY = NoiseUtil.floor(y) + 1;

        float distance = 0f;
        for (int cy = maxY - 2; cy <= maxY; cy++) {
            for (int cx = maxX - 2; cx <= maxX; cx++) {
                float dist = getRiverDistance(x, y, cx, cy);

                distance = Math.max(distance, dist);
            }
        }

        return 1 - distance;
    }

    public void carve(float x, float y, NoiseSample sample) {
        var riverSample = localRiverSample.get().reset();
        sample(x, y, riverSample);

        sample.riverNoise = getRiverNoise(riverSample);
        sample.baseNoise = getBaseNoise(sample, riverSample);
        sample.heightNoise = getRiverHeight(sample);
    }

    public void sample(float x, float y, RiverSample sample) {
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

        RiverNode nearest = null;

        float projection = 0;
        float distance2 = Float.MAX_VALUE;

        for (int cy = minY; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++) {
                var nodes = getNodes(cx, cy);

                for (var node : nodes) {
                    float t = node.getProjection(x, y);
                    float d = node.getDistance2(x, y, t);

                    if (d < distance2) {
                        nearest = node;
                        distance2 = d;
                        projection = t;
                    }
                }
            }
        }

        if (nearest != null) {
            float level = nearest.getHeight(projection);
            float radius = nearest.getRadius(projection);
            sample.projection = projection;
            sample.distance = NoiseUtil.sqrt(distance2);
            sample.radius = radius;
            sample.level = continent.shapeGenerator.getBaseNoise(level);
        } else {
            sample.reset();
        }
    }

    public float getBaseNoise(NoiseSample sample, RiverSample riverSample) {
        float level = sample.baseNoise;
        float modifier = getBaseModifier(sample);
        if (Float.isNaN(riverSample.distance)) return level * modifier;

        float distance = riverSample.distance;
        float nodeLevel = riverSample.level;
        float nodeRadius = riverSample.radius;

        if (distance >= blendRadius) return level * modifier;
        if (distance <= nodeRadius) return nodeLevel * modifier;
        if (level < nodeLevel) return level * modifier;

        float alpha = (distance - nodeRadius) / (blendRadius - nodeRadius);

        return NoiseUtil.lerp(nodeLevel, level, alpha) * modifier;
    }

    public float getBaseModifier(NoiseSample sample) {
        float min = ContinentPoints.COAST;
        float max = 1.0f;
        return NoiseUtil.map(sample.continentNoise, min, max, max - min);
    }

    public float getRiverNoise(RiverSample sample) {
        return sample.distance >= sample.radius ? 1.0f : sample.distance / sample.radius;
    }

    public float getRiverHeight(NoiseSample sample) {
        var levels = continent.levels;

        float base = sample.baseNoise;
        float river = sample.riverNoise;
        float height = sample.heightNoise;

        float baseLevel = levels.toHeightNoise(base, 0f);
        float bedLevel = baseLevel + levels.scale(-3);
        float banksLevel = baseLevel + levels.scale(2);

        float bankWidth = 0.1f;
        float bedWidth = 0.025f;

        if (river < 1 && banksLevel < height) {
            float alpha = NoiseUtil.map(river, bankWidth, 1.0f, 1.0f - bankWidth);
            height = NoiseUtil.lerp(banksLevel, height, alpha);
        }

        if (river < bankWidth && bedLevel < height) {
            float alpha = NoiseUtil.map(river, bedWidth, bankWidth, bankWidth - bedWidth);
            height = NoiseUtil.lerp(bedLevel, height, alpha);
            sample.terrainType = TerrainType.RIVER;
        }

        return height;
    }

    private float getRiverDistance(float x, float y, int cx, int cy) {
        var nodes = getNodes(cx, cy);

        float distance = 0f;
        for (var node : nodes) {
            float t = node.getProjection(x, y);
            float dist = node.getDistance(x, y, t);
            distance = Math.max(distance, dist);
        }

        return distance;
    }

    private RiverNode[] getNodes(int x, int y) {
        long index = PosUtil.pack(x, y);
        return cache.computeIfAbsent(index, this::computeNodes);
    }

    private RiverNode[] computeNodes(long index) {
        int ax = PosUtil.unpackLeft(index);
        int ay = PosUtil.unpackRight(index);

        var a = continent.getCell(ax, ay);
        var min = a;

        float ah = getHeight(a.noise(), 0, 1);
        float ar = getRadius(a.noise(), minRadius, maxRadius);

        boolean isSource = true;

        var list = localBuilder.get();
        for (var dir : DIRS) {
            int bx = ax + dir.x;
            int by = ay + dir.y;
            var b = continent.getCell(bx, by);

            // Track the lowest neighbour as the candidate to connect A to
            if (b.noise() < min.noise()) {
                min = b;
                continue;
            }

            // Check if B is higher and A is its lowest neighbour
            if (connects(a, b, ax, ay, dir.x, dir.y)) {
                float bh = getHeight(b.noise(), 0, 1);
                float br = getRadius(b.noise(), minRadius, maxRadius);
                list.add(RiverNode.of(a, b, ah, bh, ar, br));
                isSource = false;
            }
        }

        // Add connection if we found a neighbour that is lower than A
        // TODO: add a lake if there is no lower neighbour
        if (min != a) {
            float bh = getHeight(min.noise(), 0, 1);
            float br = getRadius(min.noise(), minRadius, maxRadius);
            list.add(RiverNode.of(a, min, ah, bh, ar, br));
        }

        if (isSource) {
            // TODO: add a lake if no rivers flow into A
        }

        var nodes = list.toArray(RiverNode[]::new);

        list.clear();

        return nodes;
    }

    private boolean connects(CellPoint a, CellPoint b, int x, int y, int dx, int dy) {
        var connection = b;

        for (var dir : DIRS) {
            int cx = x + dx + dir.x;
            int cy = y + dy + dir.y;

            if (cx == x && cy == y) continue;

            var c = continent.getCell(cx, cy);
            if (c.noise() < connection.noise()) {
                connection = c;
            }
        }

        return connection == a;
    }

    private float getHeight(float noise, float min, float max) {
        return noise;
    }

    private float getRadius(float noise, float min, float max) {
        float lower = continent.shapeGenerator.threshold;
        float upper = continent.shapeGenerator.threshold + 0.15f;

        noise = NoiseUtil.map(noise, lower, upper, (upper - lower));
        noise = 1 - noise;

        return min + noise * (max - min);
    }
}
