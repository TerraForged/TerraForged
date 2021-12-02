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

package com.terraforged.mod.worldgen.biome.vegetation;

import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class TreePositionSampler {
    protected static final float BORDER = 6F;
    protected static final int OFFSET_START = 23189045;

    public static boolean place(long seed,
                                float freq,
                                float jitter,
                                ChunkAccess chunk,
                                WorldGenLevel level,
                                Generator generator,
                                WorldgenRandom random,
                                FeatureDecorator decorator) {

        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        return place(seed, x, z, decorator, level, chunk, generator, random, freq, jitter);
    }

    protected static boolean place(long seed,
                                   int x0, int z0,
                                   FeatureDecorator decorator,
                                   WorldGenLevel level,
                                   ChunkAccess chunk,
                                   Generator generator,
                                   WorldgenRandom random,
                                   float freq,
                                   float jitter) {

        int worldSeed = (int) level.getSeed();

        int chunkX = x0 >> 4;
        int chunkZ = z0 >> 4;

        int minX = NoiseUtil.floor((x0 - BORDER) * freq);
        int minZ = NoiseUtil.floor((z0 - BORDER) * freq);
        int maxX = NoiseUtil.floor((x0 + 15 + BORDER) * freq);
        int maxZ = NoiseUtil.floor((z0 + 15 + BORDER) * freq);

        boolean placed = false;
        var pos = new BlockPos.MutableBlockPos();
        var terrainData = generator.getChunkData(chunk.getPos());

        int offset = OFFSET_START;
        for (int z = minZ; z <= maxZ; z++) {
            float offX = (z & 1) * 0.5F;

            for (int x = minX; x <= maxX; x++) {
                int hash = MathUtil.hash(worldSeed, x, z);
                float ox = MathUtil.randX(hash);
                float oz = MathUtil.randZ(hash);

                float px = x + offX + (ox * jitter * 0.65F);
                float pz = z + (oz * jitter);

                int posX = NoiseUtil.floor(px / freq);
                int posZ = NoiseUtil.floor(pz / freq);

                if (posX >> 4 != chunkX || posZ >> 4 != chunkZ) continue;

                pos.set(posX, 0, posZ);

                boolean result = placeAt(seed, ++offset, hash, pos, decorator, chunk, terrainData, level, generator, random);

                placed |= result;
            }
        }

        return placed;
    }

    protected static boolean placeAt(long seed,
                                     int offset,
                                     int hash,
                                     BlockPos.MutableBlockPos pos,
                                     FeatureDecorator decorator,
                                     ChunkAccess chunk,
                                     TerrainData terrainData,
                                     WorldGenLevel level,
                                     Generator generator,
                                     WorldgenRandom random) {

        pos.setY(chunk.getHeight(Heightmap.Types.WORLD_SURFACE_WG, pos.getX(), pos.getZ()));

        var biome = level.getBiome(pos);

        var manager = decorator.getVegetationManager();
        var vegetation = manager.getVegetation(biome);
        var viability = manager.getViability(biome);

        float density = viability.density();
        float value = viability.viability().getFitness(pos.getX(), pos.getZ(), terrainData, generator);
        if (value * density < MathUtil.rand(hash)) return false;

        for (var feature : vegetation.getTrees()) {
            random.setFeatureSeed(seed, offset, BiomeVegetation.STAGE);
            if (feature.place(level, generator, random, pos)) {
//                return true;
            }
        }

        return false;
    }

    public static void main(String[] args) {

    }
}
