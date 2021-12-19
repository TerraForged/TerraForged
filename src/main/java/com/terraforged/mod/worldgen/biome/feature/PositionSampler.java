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
import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.biome.vegetation.VegetationFeatures;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
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

        for (int i = 0; i < context.biomeList.size(); i++) {
            var biome = context.biomeList.get(i);
            var vegetation = decorator.getVegetationManager().getVegetation(biome);
            var config = vegetation.config;
            context.push(biome, vegetation);

            sample(seed, x, z, config.frequency(), config.jitter(), context, PositionSampler::placeAt);

            placeGrass(seed, x, z, context);
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

    public static <T> boolean sample(long seed, int x, int z, float freq, float jitter, T context, Sampler<T> sampler) {
        float freqX = freq;
        float freqZ = freq * SQUASH_FACTOR;
        int minX = NoiseUtil.floor((x - BORDER) * freqX);
        int minZ = NoiseUtil.floor((z - BORDER) * freqZ);
        int maxX = NoiseUtil.floor((x + 15 + BORDER) * freqX);
        int maxZ = NoiseUtil.floor((z + 15 + BORDER) * freqZ);
        return sample(seed, minX, minZ, maxX, maxZ, freqX, freqZ, jitter, context, sampler);
    }

    public static <T> boolean sample(long seed, int minX, int minZ, int maxX, int maxZ, float freqX, float freqZ, float jitter, T context, Sampler<T> sampler) {
        int cellSeed = (int) seed;
        int offset = OFFSET_START;

        boolean result = false;
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

                boolean sampleResult = sampler.sample(seed, offset++, hash, posX, posZ, context);
                result |= sampleResult;
            }
        }

        return result;
    }

    private static void setMaterial(int x, int y, int z, float viability, SamplerContext context) {
        var pos = context.pos.set(x, y, z);
        var state = context.chunk.getBlockState(pos);

        if (state.getBlock() == Blocks.GRASS_BLOCK) {
            if (viability < 0) return;

//            state = Blocks.BLACK_WOOL.defaultBlockState();
//            if (viability < 0.1) {
//                state = Blocks.BROWN_WOOL.defaultBlockState();
//            } else if (viability < 0.2) {
//                state = Blocks.RED_WOOL.defaultBlockState();
//            } else if (viability < 0.3) {
//                state = Blocks.PINK_WOOL.defaultBlockState();
//            } else if (viability < 0.4) {
//                state = Blocks.GRAVEL.defaultBlockState();
//            } else if (viability < 0.5) {
//                state = Blocks.STONE.defaultBlockState();
//            } else if (viability < 0.6) {
//                state = Blocks.GRAVEL.defaultBlockState();
//            } else if (viability < 0.7) {
//                state = Blocks.GREEN_WOOL.defaultBlockState();
//            }
//
//            context.chunk.setBlockState(pos, state, false);
        }
    }

    private static boolean placeAt(long seed, int offset, int hash, int x, int z, SamplerContext context) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkX != context.chunk.getPos().x || chunkZ != context.chunk.getPos().z) return false;

        int y = context.chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
        if (y <= context.generator.getSeaLevel()) return false;

        context.pos.set(x, y, z);

        var biome = context.region.getBiome(context.pos);
        if (biome != context.biome) return false;

        float viability = context.viability.get(x & 15, z & 15);
        float noise = (1 - context.vegetation.density()) * MathUtil.rand(hash);
        if (viability < noise) return false;

        for (var feature : context.features.trees()) {
            context.random.setFeatureSeed(seed, offset, VegetationFeatures.STAGE);

            feature.placeWithBiomeCheck(context.region, context.generator, context.random, context.pos);
        }

        return false;
    }

    private static void placeGrass(long seed, int x, int z, SamplerContext context) {
        int offset = OFFSET_START + 7812634;

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
            int j = 0;

            for (var feature : context.features.grass()) {
                random.setFeatureSeed(seed, offset + i + j, VegetationFeatures.STAGE);
                feature.placeWithBiomeCheck(region, generator, random, pos);
                j++;
            }
        }
    }

    public interface Sampler<T> {
        boolean sample(long seed, int offset, int hash, int x, int z, T ctx);
    }
}
