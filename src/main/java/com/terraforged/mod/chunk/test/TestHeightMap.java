package com.terraforged.mod.chunk.test;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.terrain.Terrains;

public class TestHeightMap extends Heightmap {

    private final Terrains terrains;

    public TestHeightMap(GeneratorContext context) {
        super(context);
        terrains = context.terrain;
        System.out.println("TESTETETEST");
    }

    @Override
    public void applyBase(Cell cell, float x, float y) {
        continentGenerator.apply(cell, x, y);
        regionModule.apply(cell, x, y);

        Populator populator = getPopulator(Test.getTerrainType(terrains), Test.getTerrainVariant());
        if (populator == this) {
            return;
        }

        populator.apply(cell, x, y);
        applyClimate(cell, x, y);
    }
}
