package com.terraforged.core.region.chunk;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

public interface ChunkReader extends ChunkHolder {

    Cell<Terrain> getCell(int dx, int dz);

    @Override
    default void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor) {
        int regionMinX = getBlockX();
        int regionMinZ = getBlockZ();
        if (maxX < regionMinX || maxZ < regionMinZ) {
            return;
        }

        int regionMaxX = getBlockX() + 15;
        int regionMaxZ = getBlockZ() + 15;
        if (minX > regionMaxX || maxZ > regionMaxZ) {
            return;
        }

        minX = Math.max(minX, regionMinX);
        minZ = Math.max(minZ, regionMinZ);
        maxX = Math.min(maxX, regionMaxX);
        maxZ = Math.min(maxZ, regionMaxZ);

        for (int z = minZ; z <= maxX; z++) {
            for (int x = minX; x <= maxZ; x++) {
                visitor.visit(getCell(x, z), x, z);
            }
        }
    }

    default void iterate(Cell.Visitor<Terrain> visitor) {
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                visitor.visit(getCell(dx, dz), dx, dz);
            }
        }
    }
}
