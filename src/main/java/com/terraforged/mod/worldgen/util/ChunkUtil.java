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

package com.terraforged.mod.worldgen.util;

import com.terraforged.mod.worldgen.terrain.StructureTerrain;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkUtil {
    protected static final ThreadLocal<Biome[]> BIOME_BUFFER_2D = ThreadLocal.withInitial(() -> new Biome[4 * 4]);

    public static void fillNoiseBiomes(ChunkAccess chunk, BiomeSource source, Climate.Sampler sampler) {
        var pos = chunk.getPos();
        int biomeX = QuartPos.fromBlock(pos.getMinBlockX());
        int biomeZ = QuartPos.fromBlock(pos.getMinBlockZ());
        var heightAccessor = chunk.getHeightAccessorForGeneration();

        var biomeBuffer = BIOME_BUFFER_2D.get();
        for (int dz = 0; dz < 4; dz++) {
            for (int dx = 0; dx < 4; dx++) {
                var biome = source.getNoiseBiome(biomeX + dx, 0, biomeZ + dz, sampler);

                biomeBuffer[dz << 2 | dx] = biome;
            }
        }

        for(int i = heightAccessor.getMinSection(); i < heightAccessor.getMaxSection(); ++i) {
            var chunkSection = chunk.getSection(chunk.getSectionIndexFromSectionY(i));
            fillNoiseBiomes(chunkSection, biomeBuffer);
        }
    }

    private static void fillNoiseBiomes(LevelChunkSection section, Biome[] biomeBuffer) {
        var biomes = section.getBiomes();
        biomes.acquire();
        for (int dz = 0; dz < 4; dz++) {
            for (int dx = 0; dx < 4; dx++) {
                var biome = biomeBuffer[dz << 2 | dx];
                for (int dy = 0; dy < 4; dy++) {
                    biomes.getAndSetUnchecked(dx, dy, dz, biome);
                }
            }
        }
        biomes.release();
    }

    public static void fillChunk(int seaLevel, ChunkAccess chunk, TerrainData terrainData, FillerBlock filler) {
        int max = Math.max(seaLevel, terrainData.getMax());

        for (int sy = chunk.getMinBuildHeight(); sy < max; sy += 16) {
            int index = chunk.getSectionIndex(sy);
            var section = chunk.getSection(index);
            fillSection(sy, seaLevel, terrainData, chunk, section, filler);
        }
    }

    public static void primeHeightmaps(int seaLevel, ChunkAccess chunk, TerrainData terrainData, FillerBlock filler) {
        var solid = Blocks.STONE.defaultBlockState();
        var oceanFloor = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.OCEAN_FLOOR_WG);
        var worldSurface = chunk.getOrCreateHeightmapUnprimed(Heightmap.Types.WORLD_SURFACE_WG);

        for (int z = 0, i = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++, i++) {
                int floor = terrainData.getHeight(x, z);
                int surface = Math.max(seaLevel, floor);
                var surfaceBlock = filler.getState(surface, floor);
                oceanFloor.update(x, floor, z, solid);
                worldSurface.update(x, surface, z, surfaceBlock);
            }
        }
    }

    public static void buildStructureTerrain(ChunkAccess chunk, TerrainData terrainData, StructureFeatureManager structureFeatures) {
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        var operation = new StructureTerrain(chunk, structureFeatures);

        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                operation.modify(x + dx, z + dz, chunk, terrainData);
            }
        }
    }

    private static void fillSection(int startY, int seaLevel, TerrainData terrainData, ChunkAccess chunk, LevelChunkSection section, FillerBlock filler) {
        section.acquire();

        int endY = startY + 16;
        for (int z = 0, i = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++, i++) {
                int terrainHeight = terrainData.getHeight(x, z);
                int columnHeight = Math.max(terrainHeight, seaLevel);

                int maxY = Math.min(endY, columnHeight);
                for (int y = startY; y < maxY; y++) {
                    var state = filler.getState(y, terrainHeight);

                    section.setBlockState(x, y & 15, z, state, false);

                    if (state.getLightEmission() != 0 && chunk instanceof ProtoChunk proto) {
                        proto.addLight(new BlockPos(x, y, z));
                    }
                }
            }
        }

        section.release();
    }

    public interface FillerBlock {
        BlockState getState(int y, int height);
    }
}
