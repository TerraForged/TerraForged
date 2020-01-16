package com.terraforged.core.region.chunk;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

public interface ChunkWriter extends ChunkHolder {

    Cell<Terrain> genCell(int dx, int dz);

    default void generate(Cell.Visitor<Terrain> visitor) {
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                visitor.visit(genCell(dx, dz), dx, dz);
            }
        }
    }
}
