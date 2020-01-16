package com.terraforged.core.cell;

import com.terraforged.core.world.terrain.Terrain;

public interface Extent {

    Cell<Terrain> getCell(int x, int z);

    void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor);
}
