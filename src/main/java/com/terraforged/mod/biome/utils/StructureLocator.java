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

    public static BlockPos findStructure(TFChunkGenerator generator, IWorld world, StructureManager manager, Structure<?> structure, BlockPos center, int attempts, boolean first, StructureSeparationSettings settings) {
        long seed = generator.getSeed();
        int separation = settings.func_236668_a_();
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;

        SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
        TFBiomeProvider biomeProvider = generator.getBiomeProvider();

        try (Resource<Cell> cell = Cell.pooled()) {
            for (int radius = 0; radius <= attempts; ++radius) {
                for (int dx = -radius; dx <= radius; ++dx) {
                    boolean flag = dx == -radius || dx == radius;

                    for (int dz = -radius; dz <= radius; ++dz) {
                        boolean flag1 = dz == -radius || dz == radius;
                        if (flag || flag1) {
                            int x = chunkX + separation * dx;
                            int z = chunkZ + separation * dz;

                            Biome biome = biomeProvider.lookupBiome(cell.get(), x, z);
                            if (!biome.getGenerationSettings().hasStructure(structure)) {
                                continue;
                            }

                            ChunkPos chunkpos = structure.getChunkPosForStructure(settings, seed, sharedseedrandom, x, z);
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

                            if (radius == 0) {
                                break;
                            }
                        }
                    }

                    if (radius == 0) {
                        break;
                    }
                }
            }
        }
        return null;
    }
}
