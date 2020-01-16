package com.terraforged.core.module;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrain;

public class CellLookup implements Populator {

    private final int scale;
    private final Heightmap heightmap;

    public CellLookup(Heightmap heightmap, int scale) {
        this.heightmap = heightmap;
        this.scale = scale;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        float px = x * scale;
        float pz = y * scale;
        heightmap.getValue(px, pz);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {

    }
}
