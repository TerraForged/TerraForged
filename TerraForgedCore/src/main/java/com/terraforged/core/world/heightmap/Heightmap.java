package com.terraforged.core.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.cell.Extent;
import com.terraforged.core.region.Size;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.river.RiverManager;
import com.terraforged.core.world.terrain.Terrain;

import java.rmi.UnexpectedException;

public interface Heightmap extends Populator, Extent {

    Climate getClimate();

    RiverManager getRiverManager();

    @Override
    default Cell<Terrain> getCell(int x, int z) {
        throw new RuntimeException("Don't use this pls");
    }

    @Override
    default void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int chunkSize = Size.chunkToBlock(1);

        int chunkMinX = Size.blockToChunk(minX);
        int chunkMinZ = Size.blockToChunk(minZ);
        int chunkMaxX = Size.blockToChunk(maxX);
        int chunkMaxZ = Size.blockToChunk(maxZ);

        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            for (int chunkZ = chunkMinZ; chunkZ <= chunkMaxZ; chunkZ++) {
                for (int chunkX = chunkMinX; chunkX <= chunkMaxX; chunkX++) {
                    int chunkStartX = Size.chunkToBlock(chunkX);
                    int chunkStartZ = Size.chunkToBlock(chunkZ);
                    for (int dz = 0; dz < chunkSize; dz++) {
                        for (int dx = 0; dx < chunkSize; dx++) {
                            int x = chunkStartX + dx;
                            int z = chunkStartZ + dz;
                            apply(cell.getValue(), x, z);
                            if (x >= minX && x < maxX && z >= minZ && z < maxZ) {
                                int relX = x - minX;
                                int relZ = z - minZ;
                                visitor.visit(cell.getValue(), relX, relZ);
                            }
                        }
                    }
                }
            }
        }
    }
}
