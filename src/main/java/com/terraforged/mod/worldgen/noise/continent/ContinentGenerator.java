package com.terraforged.mod.worldgen.noise.continent;/*
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

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.SpiralIterator;
import com.terraforged.mod.util.map.LossyCache;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

public class ContinentGenerator {
    private final int seed;
    private final float jitter;
    private final float threshold;
    private final Module falloff;
    private final float centreX, centreY;
    private final ThreadLocal<long[]> buffer = ThreadLocal.withInitial(() -> new long[25]);
    private final LossyCache<Mesh> meshCache = new LossyCache.Concurrent<>(1024, Mesh[]::new, m -> {});

    public ContinentGenerator(int seed, float jitter, float threshold, Module falloff) {
        long centre = findCentre(100_000, seed, jitter, threshold);

        this.seed = seed;
        this.jitter = jitter;
        this.threshold = threshold;
        this.falloff = falloff;

        this.centreX = PosUtil.unpackLeftf(centre);
        this.centreY = PosUtil.unpackRightf(centre);
    }

    public float getEdgeValue(float x, float y) {
        x += centreX;
        y += centreY;

        int ix = NoiseUtil.floor(x);
        int iy = NoiseUtil.floor(y);

        float min0 = Float.MAX_VALUE;
        float min1 = Float.MAX_VALUE;
        var data = buffer.get();

        for (int cy = iy - 2, i = 0; cy <= iy + 2; cy++) {
            for (int cx = ix - 2; cx <= ix + 2; cx++, i++) {
                int hash = MathUtil.hash(seed, cx, cy);
                float px = MathUtil.getPosX(hash, cx, jitter);
                float py = MathUtil.getPosY(hash, cy, jitter);

                float value = getDensity(hash, threshold);
                float dist = NoiseUtil.sqrt(NoiseUtil.dist2(x, y, px, py));

                data[i] = PosUtil.packf(value, dist);

                if (dist < min0) {
                    min1 = min0;
                    min0 = dist;
                } else if (dist < min1) {
                    min1 = dist;
                }
            }
        }

        float value = Source.simplex(seed + 12963845, 5, 1).clamp(0.1, 0.9).map(0.01, 0.5).getValue(x, y);

        return getEdge(min0, min1, value, data);
    }

    public float getRiverValue(float x, float y) {
        return getRiverValue(x, y, getNearest(x, y));
    }

    public float getRiverValue(float x, float y, long index) {
        if (index == Long.MAX_VALUE) return 1f;

        x += centreX;
        y += centreY;

        final float radiusB = 0.25f;
        final float radiusA = 0.25f;

        float distance = Float.MAX_VALUE;
        for (var edge : getMesh(index).rivers) {
            float d2 = edge.distance(x, y, radiusA, radiusB);
            distance = Math.min(distance, d2);
        }

        return NoiseUtil.clamp(distance, 0, 1);
    }

    public long getNearest(float x, float y) {
        x += centreX;
        y += centreY;

        int xi = NoiseUtil.floor(x);
        int yi = NoiseUtil.floor(y);

        int nx = 0, ny = 0, nHash = 0;
        float distance2 = Float.MAX_VALUE;

        for (int cy = yi - 1; cy <= yi + 1; cy++) {
            for (int cx = xi - 1; cx <= xi + 1; cx++) {
                int hash = MathUtil.hash(seed, cx, cy);
                float px = MathUtil.getPosX(hash, cx, jitter);
                float py = MathUtil.getPosY(hash, cy, jitter);
                float d2 = NoiseUtil.dist2(x, y, px, py);

                if (d2 < distance2) {
                    distance2 = d2;
                    nHash = hash;
                    nx = cx;
                    ny = cy;
                }
            }
        }

        if (getDensity(nHash, threshold) == 0) return Long.MAX_VALUE;

        return PosUtil.pack(nx, ny);
    }

    public Mesh computeMesh(long index) {
        int cx = PosUtil.unpackLeft(index);
        int cy = PosUtil.unpackRight(index);

        int hash = MathUtil.hash(seed, cx, cy);
        float px = MathUtil.getPosX(hash, cx, jitter);
        float py = MathUtil.getPosY(hash, cy, jitter);

        var mesh = new Mesh(hash, cx, cy, px, py);
        mesh.init(seed, jitter, threshold);

        return mesh;
    }

    public Mesh getMesh(long index) {
        return meshCache.computeIfAbsent(index, this::computeMesh);
    }

    public static int getDensity(int hash, float threshold) {
        return MathUtil.rand(hash) > threshold ? 1 : 0;
    }

    private static float getEdge(float min0, float min1, float falloff, long[] data) {
        float borderDistance = (min0 + min1) * 0.5F;
        float blendRadius = borderDistance * falloff;

        float sumValue = 0f;
        float sumWeight = 0f;

        for (long l : data) {
            float value = PosUtil.unpackLeftf(l);
            float distance = PosUtil.unpackRightf(l);
            float weight = getWeight(distance, min0, blendRadius);
            sumValue += value * weight;
            sumWeight += weight;
        }

        return NoiseUtil.clamp(sumValue / sumWeight, 0, 1);
    }

    public static long findCentre(int radius, int seed, float jitter, float threshold) {
        var iter = new SpiralIterator(0, 0, 0, radius);

        while (iter.hasNext()) {
            long next = iter.next();

            int x = PosUtil.unpackLeft(next);
            int y = PosUtil.unpackRight(next);
            int hash = MathUtil.hash(seed, x, y);
            if (getDensity(hash, threshold) == 1) {
                float px = MathUtil.getPosX(hash, x, jitter);
                float py = MathUtil.getPosY(hash, y, jitter);
                return PosUtil.packf(px, py);
            }
        }

        return 0L;
    }

    private static float getWeight(float dist, float origin, float blendRange) {
        float delta = dist - origin;
        if (delta <= 0) return 1F;
        if (delta >= blendRange) return 0F;
        return 1 - (delta / blendRange);
    }
}
