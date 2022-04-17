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

package com.terraforged.mod.worldgen.terrain;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.util.SpiralIterator;
import com.terraforged.mod.util.map.Object2FloatCache;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.Seedable;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;

public class TerrainBlender implements Module, Seedable<TerrainBlender> {
    private static final int REGION_SEED_OFFSET = 21491124;
    private static final int WARP_SEED_OFFSET = 12678;

    private final int regionSeed;
    private final int scale;
    private final float frequency;
    private final float jitter;
    private final float blending;

    private final Domain warp;
    private final WeightMap<TerrainNoise> terrains;
    private final ThreadLocal<Blender> localBlender = ThreadLocal.withInitial(Blender::new);

    public TerrainBlender(long seed, int scale, float jitter, float blending, TerrainNoise[] terrains) {
        this.regionSeed = (int) seed + REGION_SEED_OFFSET;
        this.scale = scale;
        this.frequency = 1F / scale;
        this.jitter = jitter;
        this.blending = blending;
        this.terrains = WeightMap.of(terrains);
        this.warp = Domain.warp(Source.SIMPLEX, (int) seed + WARP_SEED_OFFSET, scale, 3,scale / 2.5F);
    }

    @Override
    public TerrainBlender withSeed(long seed) {
        var input = terrains.getValues();
        var output = new TerrainNoise[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = input[i].withSeed(seed);
        }
        return new TerrainBlender(seed, scale, jitter, blending, output);
    }

    @Override
    public float getValue(float x, float z) {
        var blender = localBlender.get();
        return getValue(x, z, blender);
    }

    public float getValue(float x, float z, Blender blender) {
        float rx = warp.getX(x, z) * frequency;
        float rz = warp.getY(x, z) * frequency;
        getCell(regionSeed, rx, rz, jitter, blender);
        return blender.getValue(x, z, blending, terrains);
    }

    public Blender getBlenderResource() {
        return localBlender.get();
    }

    public Terrain getTerrain(Blender blender) {
        float index = blender.getCentreNoiseIndex();
        return terrains.getValue(index).terrain();
    }

    public SpiralIterator.PositionFinder findNearest(float x, float z, int minRadius, int maxRadius, Terrain type) {
        var terrain = terrains.find(t -> t.terrain() == type);
        if (terrain == null) return null;

        long band = terrains.getBand(terrain);
        float lower = PosUtil.unpackLeftf(band);
        float upper = PosUtil.unpackRightf(band);

        return iterator(x, z, minRadius, maxRadius).finder(it -> {
            long pos = find(regionSeed, jitter, lower, upper, it);
            float px = PosUtil.unpackLeftf(pos) / frequency;
            float pz = PosUtil.unpackRightf(pos) / frequency;
            return PosUtil.packf(px, pz);
        });
    }

    public SpiralIterator iterator(float x, float z, int min, int max) {
        float rx = warp.getX(x, z) * frequency;
        float rz = warp.getY(x, z) * frequency;

        int cx = NoiseUtil.floor(rx);
        int cz = NoiseUtil.floor(rz);

        return new SpiralIterator(cx, cz, min, max);
    }

    private static long find(int seed, float jitter, float lower, float upper, SpiralIterator iterator) {
        while (iterator.hasNext()) {
            long next = iterator.next();
            int cx = PosUtil.unpackLeft(next);
            int cz = PosUtil.unpackRight(next);

            int hash = NoiseUtil.hash2D(seed, cx, cz);
            float noise = MathUtil.rand(hash);

            if (noise > lower && noise <= upper) {
                float dx = MathUtil.rand(hash, NoiseUtil.X_PRIME);
                float dz = MathUtil.rand(hash, NoiseUtil.Y_PRIME);

                float px = cx + dx * jitter;
                float pz = cz + dz * jitter;

                return PosUtil.packf(px, pz);
            }
        }
        return 0L;
    }

    private static void getCell(int seed, float x, float z, float jitter, Blender blender) {
        int maxX = NoiseUtil.floor(x) + 1;
        int maxZ = NoiseUtil.floor(z) + 1;

        blender.closestIndex = 0;
        blender.closestIndex2 = 0;

        int nearestIndex = -1;
        int nearestIndex2 = -1;

        float nearestDistance = Float.MAX_VALUE;
        float nearestDistance2 = Float.MAX_VALUE;

        for (int cz = maxZ - 2, i = 0; cz <= maxZ; cz++) {
            for (int cx = maxX - 2; cx <= maxX; cx++, i++) {
                int hash = NoiseUtil.hash2D(seed, cx, cz);

                float dx = MathUtil.rand(hash, NoiseUtil.X_PRIME);
                float dz = MathUtil.rand(hash, NoiseUtil.Y_PRIME);

                float px = cx + dx * jitter;
                float pz = cz + dz * jitter;
                float dist2 = NoiseUtil.dist2(x, z, px, pz);

                blender.hashes[i] = hash;
                blender.distances[i] = dist2;

                if (dist2 < nearestDistance) {
                    nearestDistance2 = nearestDistance;
                    nearestDistance = dist2;
                    nearestIndex2 = nearestIndex;
                    nearestIndex = i;
                } else if (dist2 < nearestDistance2) {
                    nearestDistance2 = dist2;
                    nearestIndex2 =  i;
                }
            }
        }

        blender.closestIndex = nearestIndex;
        blender.closestIndex2 = nearestIndex2;
    }

    public static class Blender {
        protected int closestIndex;
        protected int closestIndex2;

        protected final int[] hashes = new int[9];
        protected final float[] distances = new float[9];
        protected final Object2FloatCache<TerrainNoise> cache = new Object2FloatCache<>(9);

        public float getCentreNoiseIndex() {
            return getNoiseIndex(closestIndex);
        }

        public float getDistance(int index) {
            return NoiseUtil.sqrt(distances[index]);
        }

        public float getCentreValue(float x, float z, WeightMap<TerrainNoise> terrains) {
            float noise = getCentreNoiseIndex();
            return terrains.getValue(noise).noise().getValue(x, z);
        }

        public float getValue(float x, float z, float blending, WeightMap<TerrainNoise> terrains) {
            float dist0 = getDistance(closestIndex);
            float dist1 = getDistance(closestIndex2);

            float borderDistance = (dist0 + dist1) * 0.5F;
            float blendRadius = borderDistance * blending;
            float blendStart = borderDistance - blendRadius;

            if (dist0 <= blendStart) {
                return getCentreValue(x, z, terrains);
            } else {
                return getBlendedValue(x, z, dist0, dist1, blendRadius, terrains);
            }
        }

        public float getBlendedValue(float x, float z, float nearest, float nearest2, float blendRange, WeightMap<TerrainNoise> terrains) {
            cache.clear();

            float sumNoise = getCacheValue(closestIndex, x, z, terrains);
            float sumWeight = getWeight(nearest, nearest, blendRange);

            float nearestWeight2 = getWeight(nearest2, nearest, blendRange);
            if (nearestWeight2 > 0) {
                sumNoise += getCacheValue(closestIndex2, x, z, terrains) * nearestWeight2;
                sumWeight += nearestWeight2;
            }

            for (int i = 0; i < 9; i++) {
                if (i == closestIndex || i == closestIndex2) continue;

                float weight = getWeight(getDistance(i), nearest, blendRange);
                if (weight > 0) {
                    sumNoise += getCacheValue(i, x, z, terrains) * weight;
                    sumWeight += weight;
                }
            }

            return NoiseUtil.clamp(sumNoise / sumWeight, 0, 1);
        }

        private float getCacheValue(int index, float x, float z, WeightMap<TerrainNoise> terrains) {
            float noiseIndex = getNoiseIndex(index);
            var terrain = terrains.getValue(noiseIndex);

            float value = cache.get(terrain);
            if (Float.isNaN(value)) {
                value = terrain.noise().getValue(x, z);
                cache.put(terrain, value);
            }

            return value;
        }

        private float getNoiseIndex(int index) {
            return MathUtil.rand(hashes[index]);
        }

        private static float getWeight(float dist, float origin, float blendRange) {
            float delta = dist - origin;
            if (delta <= 0) return 1F;
            if (delta >= blendRange) return 0F;

            float weight = 1 - (delta / blendRange);
            return weight * weight;
        }
    }
}
