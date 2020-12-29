/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.biome.utils;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.SectionPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.settings.StructureSeparationSettings;

public class StructureLocator {

    private static final int SEARCH_BATCH_SIZE = 100;

    public static BlockPos findStructure(TFChunkGenerator generator, IWorld world, StructureManager manager, Structure<?> structure, BlockPos center, int attempts, boolean first, StructureSeparationSettings settings) {
        return findStructure(generator, world, manager, structure, center, attempts, first, settings, 5_000L);
    }

    public static BlockPos findStructure(TFChunkGenerator generator, IWorld world, StructureManager manager, Structure<?> structure, BlockPos center, int radius, boolean first, StructureSeparationSettings settings, long timeout) {
        long seed = generator.getSeed();
        int separation = settings.func_236668_a_();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        TFBiomeProvider biomeProvider = generator.getBiomeProvider();

        int searchCount = 0;
        long searchTimeout = System.currentTimeMillis() + timeout;

        try (Resource<Cell> resource = Cell.pooled()) {
            Cell cell = resource.get();

            for (int dr = 0; dr <= radius; ++dr) {
                for (int dx = -dr; dx <= dr; ++dx) {
                    boolean flag = dx == -dr || dx == dr;

                    for (int dz = -dr; dz <= dr; ++dz) {
                        boolean flag1 = dz == -dr || dz == dr;
                        if (flag || flag1) {
                            int cx = chunkX + separation * dx;
                            int cz = chunkZ + separation * dz;

                            if (searchCount++ > SEARCH_BATCH_SIZE) {
                                searchCount = 0;
                                long now = System.currentTimeMillis();
                                if (now > searchTimeout) {
                                    Log.err("Structure search took too long! {}", structure.getRegistryName());
                                    return null;
                                }
                            }

                            int x = cx << 4;
                            int z = cz << 4;
                            Biome biome = biomeProvider.fastLookupBiome(cell, x, z);
                            if (!biome.getGenerationSettings().hasStructure(structure)) {
                                continue;
                            }

                            ChunkPos chunkpos = structure.getChunkPosForStructure(settings, seed, sharedseedrandom, cx, cz);
                            IChunk ichunk = world.getChunk(chunkpos.x, chunkpos.z, ChunkStatus.STRUCTURE_STARTS);
                            StructureStart<?> start = manager.getStructureStart(SectionPos.from(ichunk.getPos(), 0), structure, ichunk);
                            if (start != null && start.isValid()) {
                                if (first && start.isRefCountBelowMax()) {
                                    start.incrementRefCount();
                                    return start.getPos();
                                }

                                if (!first) {
                                    return start.getPos();
                                }
                            }

                            if (dr == 0) {
                                break;
                            }
                        }
                    }

                    if (dr == 0) {
                        break;
                    }
                }
            }
        }
        return null;
    }
}
