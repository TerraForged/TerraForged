package com.terraforged.mod.util;

import com.terraforged.mod.worldgen.terrain.StructureTerrain;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.ProtoChunk;
import net.minecraft.world.level.levelgen.Heightmap;

public class ChunkUtil {
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
