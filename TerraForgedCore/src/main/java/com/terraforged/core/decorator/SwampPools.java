package com.terraforged.core.decorator;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;

public class SwampPools implements Decorator {

    private final Module module;
    private final Levels levels;
    private final Terrains terrains;
    private final float minY;
    private final float maxY;
    private final float blendY;
    private final float blendRange;

    public SwampPools(Seed seed, Terrains terrains, Levels levels) {
        this.levels = levels;
        this.terrains = terrains;
        this.minY = levels.water(-3);
        this.maxY = levels.water(1);
        this.blendY = levels.water(4);
        this.blendRange = blendY - maxY;
        this.module = Source.perlin(seed.next(), 14, 1).clamp(0.45, 0.8).map(0, 1);
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        if (cell.tag == terrains.ocean) {
            return;
        }

        if (cell.moisture < 0.7 || cell.temperature < 0.3) {
            return;
        }

        if (cell.value <= levels.water) {
            return;
        }

        if (cell.value > blendY) {
            return;
        }

        float alpha = module.getValue(x, y);
        if (cell.value > maxY) {
            float delta = blendY - cell.value;
            float alpha2 = delta / blendRange;
            alpha *= alpha2;
        }

        cell.value = NoiseUtil.lerp(cell.value, minY, alpha);
    }
}
