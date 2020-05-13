package com.terraforged.mod.chunk.test;

import com.terraforged.core.cell.Cell;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.WorldHeightmap;
import com.terraforged.world.terrain.Terrains;

public class TestHeightMap extends WorldHeightmap {

    private final Terrains terrains;

    public TestHeightMap(GeneratorContext context) {
        super(context);
        terrains = context.terrain;
    }

    @Override
    public void apply(Cell cell, float x, float y) {
        super.apply(cell, x, y);
        getPopulator(Test.getTerrainType(terrains)).apply(cell, x, y);
    }

    @Override
    public void tag(Cell cell, float x, float y) {
        getPopulator(Test.getTerrainType(terrains)).tag(cell, x, y);
    }
}
