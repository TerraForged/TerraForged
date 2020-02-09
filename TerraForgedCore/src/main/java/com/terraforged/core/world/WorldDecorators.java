package com.terraforged.core.world;

import com.terraforged.core.decorator.Decorator;
import com.terraforged.core.decorator.DesertDunes;
import com.terraforged.core.decorator.DesertStacks;
import com.terraforged.core.decorator.SwampPools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WorldDecorators {

    private final List<Decorator> decorators;

    public WorldDecorators(GeneratorContext context) {
        context = context.copy();
        List<Decorator> list = new ArrayList<>();
        list.add(new DesertStacks(context.seed, context.levels));
        list.add(new SwampPools(context.seed, context.terrain, context.levels));
        decorators = Collections.unmodifiableList(list);
    }

    public List<Decorator> getDecorators() {
        return decorators;
    }
}
