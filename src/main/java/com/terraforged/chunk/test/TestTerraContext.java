package com.terraforged.chunk.test;

import com.terraforged.chunk.TerraContext;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.WorldGeneratorFactory;

public class TestTerraContext extends TerraContext {

    public TestTerraContext(TerraContext context) {
        super(context);
    }

    @Override
    protected WorldGeneratorFactory createFactory(GeneratorContext context) {
        return new WorldGeneratorFactory(context, new TestHeightMap(context));
    }
}
