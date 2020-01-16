package com.terraforged.core.world;

import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.heightmap.WorldHeightmap;

import java.util.function.Supplier;

public class WorldGeneratorFactory implements Supplier<WorldGenerator> {

    private final GeneratorContext context;

    private final Heightmap heightmap;
    private final WorldDecorators decorators;

    public WorldGeneratorFactory(GeneratorContext context) {
        this.context = context;
        this.heightmap = new WorldHeightmap(context);
        this.decorators = new WorldDecorators(context);
    }

    public WorldGeneratorFactory(GeneratorContext context, Heightmap heightmap) {
        this.context = context;
        this.heightmap = heightmap;
        this.decorators = new WorldDecorators(context);
    }

    public Heightmap getHeightmap() {
        return heightmap;
    }

    public Climate getClimate() {
        return getHeightmap().getClimate();
    }

    public WorldDecorators getDecorators() {
        return decorators;
    }

    @Override
    public WorldGenerator get() {
        return new WorldGenerator(heightmap, decorators, new WorldFilters(context));
    }
}
