package com.terraforged.core.util;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;

import java.util.function.BiPredicate;

public class VariablePredicate {

    private final Module module;
    private final BiPredicate<Cell<Terrain>, Float> predicate;

    public VariablePredicate(Module module, BiPredicate<Cell<Terrain>, Float> predicate) {
        this.module = module;
        this.predicate = predicate;
    }

    public boolean test(Cell<Terrain> cell, float x, float z) {
        return predicate.test(cell, module.getValue(x, z));
    }

    public static VariablePredicate height(Seed seed, Levels levels, int min, int max, int size, int octaves) {
        float bias = levels.scale(min);
        float scale = levels.scale(max - min);
        Module source = Source.perlin(seed.next(), size, 1).scale(scale).bias(bias);
        return new VariablePredicate(source, (cell, height) -> cell.value < height);
    }
}
