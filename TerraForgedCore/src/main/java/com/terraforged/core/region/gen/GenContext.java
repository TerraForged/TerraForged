package com.terraforged.core.region.gen;

import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.rivermap.RiverRegionList;

import java.util.function.Supplier;

public class GenContext {

    public final WorldGenerator generator;
    public final RiverRegionList rivers = new RiverRegionList();

    public GenContext(WorldGenerator generator) {
        this.generator = generator;
    }

    public static Supplier<GenContext> supplier(WorldGeneratorFactory factory) {
        return () -> new GenContext(factory.get());
    }
}
