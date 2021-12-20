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

import com.google.common.base.Suppliers;
import com.terraforged.mod.worldgen.GeneratorResource;
import com.terraforged.mod.worldgen.terrain.StructureTerrain;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.PalettedContainer;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;

import java.util.function.Supplier;

public class ChunkUtil {
    public static final FillerBlock FILLER = ChunkUtil::getFiller;
    public static final Supplier<ByteBuf> FULL_SECTION = Suppliers.memoize(ChunkUtil::createFullPalette);

    public static void fillNoiseBiomes(ChunkAccess chunk, BiomeSource source, Climate.Sampler sampler, GeneratorResource resource) {
        var pos = chunk.getPos();
        int biomeX = QuartPos.fromBlock(pos.getMinBlockX());
        int biomeZ = QuartPos.fromBlock(pos.getMinBlockZ());
        var heightAccessor = chunk.getHeightAccessorForGeneration();

        var biomeBuffer = resource.biomeBuffer2D;
        for (int dz = 0; dz < 4; dz++) {
            for (int dx = 0; dx < 4; dx++) {
                var biome = source.getNoiseBiome(biomeX + dx, -1, biomeZ + dz, sampler);

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

    public static void fillChunk(int seaLevel, ChunkAccess chunk, TerrainData terrainData, FillerBlock filler, GeneratorResource resource) {
        int waterMaxY = seaLevel + 1;
        int min = getLowestSection(terrainData);
        int max = getHighestColumn(waterMaxY, terrainData);

        // @Optimization Note:
        // Here, we've precomputed a full stone chunk section and written it to a bytebuffer
        // which we are then reading into each chunk section below the lowest non-full chunk
        // section (determined from our heightmap). This is waaay faster than setting blocks
        // individually in the section so helps reduce the impact of low minY values.
        var sectionData = resource.fullSection;
        for (int sy = chunk.getMinBuildHeight(); sy < min; sy += 16) {
            int index = chunk.getSectionIndex(sy);
            var section = chunk.getSection(index);
            sectionData.resetReaderIndex();
            section.getStates().read(sectionData);
            section.recalcBlockCounts();
        }

        // Here we fill the chunk section by the block
        for (int sy = min; sy < max; sy += 16) {
            int index = chunk.getSectionIndex(sy);
            var section = chunk.getSection(index);
            fillSection(sy, waterMaxY, terrainData, chunk, section, filler);
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

    private static void fillSection(int startY, int waterMaxY, TerrainData terrainData, ChunkAccess chunk, LevelChunkSection section, FillerBlock filler) {
        section.acquire();

        int sectionMaxY = startY + 16;
        for (int z = 0, i = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++, i++) {
                int solidMaxY = terrainData.getHeight(x, z) + 1;
                int surfaceMaxY = Math.max(solidMaxY, waterMaxY);

                int maxY = Math.min(sectionMaxY, surfaceMaxY);
                for (int y = startY; y < maxY; y++) {
                    var state = filler.getState(y, solidMaxY);

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

    protected static BlockState getFiller(int y, int surfaceAir) {
        return y >= surfaceAir ? Blocks.WATER.defaultBlockState() : Blocks.STONE.defaultBlockState();
    }

    protected static int getHighestColumn(int waterMaxY, TerrainData terrainData) {
        return Math.max(waterMaxY, terrainData.getMax() + 1);
    }

    protected static int getLowestSection(TerrainData terrainData) {
        int y = terrainData.getMin();
        return (y >> 4) << 4;
    }

    protected static ByteBuf createFullPalette() {
        var stateRegistry = Block.BLOCK_STATE_REGISTRY;
        var container = new PalettedContainer<>(stateRegistry, Blocks.STONE.defaultBlockState(), PalettedContainer.Strategy.SECTION_STATES);

        container.acquire();
        for (int x = 0; x < 16; x++) {
            for (int y = 0; y < 16; y++) {
                for (int z = 0; z < 16; z++) {
                    container.getAndSetUnchecked(x, y, z, Blocks.STONE.defaultBlockState());
                }
            }
        }
        container.release();

        var buffer = new FriendlyByteBuf(Unpooled.buffer());
        container.write(buffer);

        return buffer;
    }

    public static FriendlyByteBuf getFullSection() {
        return new FriendlyByteBuf(FULL_SECTION.get().copy());
    }
}
