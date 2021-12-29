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

package com.terraforged.mod.worldgen.biome.feature;

import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.biome.vegetation.VegetationFeatures;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;

import java.util.concurrent.CompletableFuture;

public class PositionSampler {
    protected static final float BORDER = 6F;
    protected static final int OFFSET_START = 23189045;
    public static final float SQUASH_FACTOR = 2F / NoiseUtil.sqrt(3);

    public static void place(long seed,
                             ChunkAccess chunk,
                             WorldGenLevel level,
                             CompletableFuture<TerrainData> terrain,
                             Generator generator,
                             WorldgenRandom random,
                             FeatureDecorator decorator) {

        var context = SamplerContext.get();
        context.chunk = chunk;
        context.region = level;
        context.random = random;
        context.generator = generator;
        context.viabilityContext.terrainData = terrain;
        context.viabilityContext.biomeSampler = generator.getBiomeSource().getBiomeSampler();
        populate(context, decorator);

        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        int offset = OFFSET_START;

        for (int i = 0; i < context.biomeList.size(); i++) {
            var biome = context.biomeList.get(i);
            var vegetation = decorator.getVegetationManager().getVegetation(biome);
            var config = vegetation.config;
            context.push(biome, vegetation);

            if (config == VegetationConfig.NONE) {
                offset = placeAt(seed, OFFSET_START, x, z, context);
            } else {
                offset = sample(seed, offset, x, z, config.frequency(), config.jitter(), context, PositionSampler::placeAt);
                offset = placeGrassAt(seed, offset, x, z, context);
            }
        }
    }

    public static void populate(SamplerContext context, FeatureDecorator decorator) {
        var chunk = context.chunk;
        int startX = chunk.getPos().getMinBlockX();
        int startZ = chunk.getPos().getMinBlockZ();

        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int x = startX + dx;
                int z = startZ + dz;
                int y = context.getHeight(dx, dz);

                var biome = context.getBiome(x, y, z);

                var vegetation = decorator.getVegetationManager().getVegetation(biome);
                var viability = vegetation.config.viability();
                float value = viability.getFitness(x, z, context.viabilityContext);

                context.viability.set(dx, dz, value);
                context.biomeList.add(biome);
            }
        }
    }

    public static <T> int sample(long seed, int offset, int x, int z, float freq, float jitter, T context, Sampler<T> sampler) {
        float freqX = freq;
        float freqZ = freq * SQUASH_FACTOR;
        int minX = NoiseUtil.floor((x - BORDER) * freqX);
        int minZ = NoiseUtil.floor((z - BORDER) * freqZ);
        int maxX = NoiseUtil.floor((x + 15 + BORDER) * freqX);
        int maxZ = NoiseUtil.floor((z + 15 + BORDER) * freqZ);
        return sample(seed, offset, minX, minZ, maxX, maxZ, freqX, freqZ, jitter, context, sampler);
    }

    public static <T> int sample(long seed, int offset, int minX, int minZ, int maxX, int maxZ, float freqX, float freqZ, float jitter, T context, Sampler<T> sampler) {
        int cellSeed = (int) seed;

        for (int pz = minZ; pz <= maxZ; pz++) {
            float ox = (pz & 1) * 0.5F;

            for (int px = minX; px <= maxX; px++) {
                int hash = MathUtil.hash(cellSeed, px, pz);
                float dx = MathUtil.randX(hash);
                float dz = MathUtil.randZ(hash);

                float sx = px + ox + (dx * jitter * 0.65F);
                float sz = pz + (dz * jitter);

                int posX = NoiseUtil.floor(sx / freqX);
                int posZ = NoiseUtil.floor(sz / freqZ);

                offset = sampler.sample(seed, offset, hash, posX, posZ, context);
            }
        }

        return offset;
    }

    private static int placeAt(long seed, int offset, int x, int z, SamplerContext context) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkX != context.chunk.getPos().x || chunkZ != context.chunk.getPos().z) return offset;

        int y = context.chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (y <= context.generator.getSeaLevel()) return offset;

        context.pos.set(x, y, z);

        for (var feature : context.features.trees()) {
            context.random.setFeatureSeed(seed, offset++, VegetationFeatures.STAGE);

            feature.placeWithBiomeCheck(context.region, context.generator, context.random, context.pos);
        }

        for (var feature : context.features.grass()) {
            context.random.setFeatureSeed(seed, offset++, VegetationFeatures.STAGE);

            feature.placeWithBiomeCheck(context.region, context.generator, context.random, context.pos);
        }

        return offset;
    }

    private static int placeAt(long seed, int offset, int hash, int x, int z, SamplerContext context) {
        if (!isFeatureChunk(x, z, context)) return offset;

        int y = context.chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (y <= context.generator.getSeaLevel()) return offset;

        context.pos.set(x, y, z);

        var biome = context.region.getBiome(context.pos);
        if (biome != context.biome) return offset;

        float viability = context.viability.get(x & 15, z & 15);
        float noise = (1 - context.vegetation.density()) * MathUtil.rand(hash);
        if (viability < noise) return offset;

        for (var feature : context.features.trees()) {
            context.random.setFeatureSeed(seed, offset++, VegetationFeatures.STAGE);

            feature.placeWithBiomeCheck(context.region, context.generator, context.random, context.pos);
        }

        return offset;
    }

    private static int placeGrassAt(long seed, int offset, int x, int z, SamplerContext context) {
        var region = context.region;
        var generator = context.generator;
        var random = context.random;
        var pos = context.pos.set(x, 0, z);

        int passes = 2;
        passes += NoiseUtil.floor(2 * (1 - context.maxViability));
        passes += NoiseUtil.floor(4 * context.terrainData().getWater().get(8, 8));
        passes -= NoiseUtil.floor(5 * context.terrainData().getHeight(8, 8));

        passes = Math.max(2, passes);

        for (int i = 0; i < passes; i++) {
            for (var feature : context.features.grass()) {
                random.setFeatureSeed(seed, offset++, VegetationFeatures.STAGE);
                feature.placeWithBiomeCheck(region, generator, random, pos);
            }
        }

        return offset;
    }

    private static boolean isFeatureChunk(int x, int z, SamplerContext context) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        return chunkX == context.chunk.getPos().x && chunkZ == context.chunk.getPos().z;
    }

    public interface Sampler<T> {
        int sample(long seed, int offset, int hash, int x, int z, T ctx);
    }
}
