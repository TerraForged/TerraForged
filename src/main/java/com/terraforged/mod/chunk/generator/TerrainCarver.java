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

package com.terraforged.mod.chunk.generator;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.mod.featuremanager.template.StructureUtils;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.ChunkCarverFix;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.carver.ConfiguredCarver;

import java.util.BitSet;
import java.util.ListIterator;
import java.util.function.Function;
import java.util.function.Supplier;

public class TerrainCarver implements Generator.Carvers {

    private final TFChunkGenerator generator;

    public TerrainCarver(TFChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public void carveTerrain(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type) {
        boolean nearRiver = nearRiver(chunk.getPos());
        boolean nearStructure = StructureUtils.hasOvergroundStructure(chunk);
        ChunkCarverFix carverChunk = new ChunkCarverFix(chunk, generator.getMaterials(), nearStructure, nearRiver);

        SharedSeedRandom random = new SharedSeedRandom();
        ChunkPos chunkpos = carverChunk.getPos();
        int chunkX = chunkpos.x;
        int chunkZ = chunkpos.z;

        int seaLevel = generator.getSeaLevel();
        BiomeLookup lookup = new BiomeLookup();
        BitSet mask = carverChunk.getCarvingMask(type);
        Biome biome = generator.getBiomeProvider().getBiome(chunkpos.getXStart(), chunkpos.getZStart());
        BiomeGenerationSettings settings = biome.getGenerationSettings();

        ListIterator<Supplier<ConfiguredCarver<?>>> iterator = settings.getCarvers(type).listIterator();
        for (int cx = chunkX - 8; cx <= chunkX + 8; ++cx) {
            for (int cz = chunkZ - 8; cz <= chunkZ + 8; ++cz) {
                while (iterator.hasNext()) {
                    int index = iterator.nextIndex();
                    ConfiguredCarver<?> carver = iterator.next().get();
                    random.setLargeFeatureSeed(generator.getSeed() + index, cx, cz);
                    if (carver.shouldCarve(random, cx, cz)) {
                        carver.carveRegion(carverChunk, lookup, random, seaLevel, cx, cz, chunkX, chunkZ, mask);
                    }
                }

                // rewind
                while (iterator.hasPrevious()) {
                    iterator.previous();
                }
            }
        }
    }

    private class BiomeLookup implements Function<BlockPos, Biome> {

        private final Cell cell = new Cell();

        @Override
        public Biome apply(BlockPos pos) {
            return generator.getBiomeProvider().lookupBiome(cell, pos.getX(), pos.getZ());
        }
    }

    private boolean nearRiver(ChunkPos pos) {
        try (ChunkReader reader = generator.getChunkReader(pos)) {
            return reader.getCell(8, 8).riverMask < 0.33F;
        }
    }
}
