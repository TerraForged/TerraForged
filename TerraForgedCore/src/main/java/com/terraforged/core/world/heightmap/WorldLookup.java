package com.terraforged.core.world.heightmap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldDecorators;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.decorator.Decorator;
import com.terraforged.core.world.terrain.Terrain;

public class WorldLookup {

    private final float waterLevel;
    private final float beachLevel;
    private final Heightmap heightmap;
    private final WorldDecorators decorators;
    private final GeneratorContext context;

    public WorldLookup(WorldGeneratorFactory factory, GeneratorContext context) {
        this.context = context;
        this.heightmap = factory.getHeightmap();
        this.decorators = factory.getDecorators();
        this.waterLevel = context.levels.water;
        this.beachLevel = context.levels.water(5);
    }

    public Cell<Terrain> getCell(int x, int z) {
        Cell<Terrain> cell = new Cell<>();

        heightmap.apply(cell, x, z);

        // approximation - actual beaches depend on steepness but that's too expensive to calculate
        if (cell.tag == context.terrain.coast && cell.value > waterLevel && cell.value <= beachLevel) {
            cell.tag = context.terrain.beach;
        }

        for (Decorator decorator : decorators.getDecorators()) {
            if (decorator.apply(cell, x, z)) {
                break;
            }
        }

        return cell;
    }
}
