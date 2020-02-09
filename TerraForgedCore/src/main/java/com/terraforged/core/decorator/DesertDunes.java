package com.terraforged.core.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.CellFunc;

public class DesertDunes implements Decorator {

    private final Module module;
    private final float climateMin;
    private final float climateMax;
    private final float climateRange;

    private final Levels levels;
    private final Terrains terrains;
    private final Terrain dunes = new Terrain("dunes", 100);

    public DesertDunes(GeneratorContext context) {
        this.climateMin = 0.6F;
        this.climateMax = 0.85F;
        this.climateRange = climateMax - climateMin;
        this.levels = context.levels;
        this.terrains = context.terrain;
        this.module = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70)
                .scale(30 / 255D);
    }

    @Override
    public boolean apply(Cell<Terrain> cell, float x, float y) {
        float temp = cell.temperature;
        float moisture = 1 - cell.moisture;
        float climate = temp * moisture;
        if (climate < climateMin) {
            return false;
        }

        float duneHeight = module.getValue(x, y);
        float climateMask = climate > climateMax ? 1F : (climate - climateMin) / climateRange;
        float regionMask = cell.mask(0.4F, 0.5F, 0,0.8F);

        float height = duneHeight * climateMask * regionMask;
        cell.value += height;
        cell.tag = dunes;

        return height >= levels.unit;
    }
}
