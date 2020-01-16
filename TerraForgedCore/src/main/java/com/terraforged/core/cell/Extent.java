package com.terraforged.core.cell;

import com.terraforged.core.world.terrain.Terrain;

public interface Extent {

    void visit(int minX, int minZ, int maxX, int maxZ, Cell.Visitor<Terrain> visitor);
}
