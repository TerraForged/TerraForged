package com.terraforged.core.module;

import me.dags.noise.Module;
import me.dags.noise.util.NoiseUtil;

public class CellLookupOffset implements Module {

    private final int scale;
    private final Module lookup;
    private final Module direction;
    private final Module strength;

    public CellLookupOffset(Module lookup, Module direction, Module strength, int scale) {
        this.scale = scale;
        this.lookup = lookup;
        this.direction = direction;
        this.strength = strength;
    }

    @Override
    public float getValue(float x, float y) {
        float px = x * scale;
        float pz = y * scale;
        float str = strength.getValue(x, y);
        float dir = direction.getValue(x, y) * NoiseUtil.PI2;
        float dx = NoiseUtil.sin(dir) * str;
        float dz = NoiseUtil.cos(dir) * str;
        return lookup.getValue(px + dx, pz + dz);
    }
}
