package com.terraforged.core.world;

import com.terraforged.core.world.heightmap.Heightmap;

public class WorldGenerator {

    private final Heightmap heightmap;
    private final WorldFilters filters;
    private final WorldDecorators decorators;

    public WorldGenerator(Heightmap heightmap, WorldDecorators decorators, WorldFilters filters) {
        this.filters = filters;
        this.heightmap = heightmap;
        this.decorators = decorators;
    }

    public Heightmap getHeightmap() {
        return heightmap;
    }

    public WorldFilters getFilters() {
        return filters;
    }

    public WorldDecorators getDecorators() {
        return decorators;
    }
}
