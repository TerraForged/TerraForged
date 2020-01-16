package com.terraforged.core.module;

import me.dags.noise.Module;

public class CellLookup implements Module {

    private final int scale;
    private final Module module;

    public CellLookup(Module module, int scale) {
        this.module = module;
        this.scale = scale;
    }

    @Override
    public float getValue(float x, float y) {
        float px = x * scale;
        float pz = y * scale;
        return module.getValue(px, pz);
    }
}
