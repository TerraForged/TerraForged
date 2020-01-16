package com.terraforged.core.module;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.util.NoiseUtil;

public class CellLookupOffset implements Populator {

    private final int scale;
    private final Module direction;
    private final Module strength;
    private final Heightmap heightmap;

    public CellLookupOffset(Heightmap heightmap, Module direction, Module strength, int scale) {
        this.scale = scale;
        this.heightmap = heightmap;
        this.direction = direction;
        this.strength = strength;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        float px = x * scale;
        float pz = y * scale;
        float str = strength.getValue(x, y);
        float dir = direction.getValue(x, y) * NoiseUtil.PI2;
        float dx = NoiseUtil.sin(dir) * str;
        float dz = NoiseUtil.cos(dir) * str;
        heightmap.visit(cell, px + dx, pz + dz);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {

    }
}
