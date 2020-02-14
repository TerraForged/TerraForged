package com.terraforged.core.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;

public class DesertStacks implements Decorator {

    private final float minY;
    private final float maxY;
    private final Levels levels;
    private final Module module;

    public DesertStacks(Seed seed, Levels levels) {
        Module mask = Source.perlin(seed.next(), 500, 1).clamp(0.7, 1).map(0, 1);

        Module shape = Source.perlin(seed.next(), 25, 1).clamp(0.6, 1).map(0, 1)
                .mult(Source.perlin(seed.next(), 8, 1).alpha(0.1));

        Module top = Source.perlin(seed.next(), 4, 1).alpha(0.25);

        Module scale = Source.perlin(seed.next(), 400, 1)
                .clamp(levels.scale(20), levels.scale(35));

        Module stack = (x, y) -> {
            float value = shape.getValue(x, y);
            if (value > 0.3) {
                return top.getValue(x, y);
            }
            return value * 0.95F;
        };

        this.minY = levels.water(0);
        this.maxY = levels.water(50);
        this.levels = levels;
        this.module = stack.scale(scale).mult(mask);
    }

    @Override
    public boolean apply(Cell<Terrain> cell, float x, float y) {
        if (BiomeType.DESERT != cell.biomeType) {
            return false;
        }

        if (cell.value <= minY || cell.value > maxY) {
            return false;
        }

        float value = module.getValue(x, y);
        value *= cell.biomeEdge;
        cell.value += value;

        return value > levels.unit;
    }
}
