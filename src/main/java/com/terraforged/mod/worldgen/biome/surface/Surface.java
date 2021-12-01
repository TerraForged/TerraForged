package com.terraforged.mod.worldgen.biome.surface;

import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

public class Surface {
    public static void apply(TerrainData terrainData, ChunkAccess chunk, ChunkGenerator generator) {
        float norm = 55 * (generator.getGenDepth() / 255F);

        var mutable = new BlockPos.MutableBlockPos();
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                int y = terrainData.getHeight(dx, dz);

                float gradient = terrainData.getGradient(dx, dz, norm);

                if (y > 60 && gradient > 0.6F) continue;

                chunk.setBlockState(mutable.set(dx, y, dz), Blocks.GRASS_BLOCK.defaultBlockState(), false);

                for (int dy = 1; dy <= 1; dy++) {
                    chunk.setBlockState(mutable.set(dx, y - dy, dz), Blocks.DIRT.defaultBlockState(), false);
                }
            }
        }
    }
}
