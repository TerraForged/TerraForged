package com.terraforged.core.world.climate;

import me.dags.noise.Module;

public class Compressor implements Module {

    private final float lowerStart;
    private final float lowerEnd;
    private final float lowerRange;
    private final float lowerExpandRange;

    private final float upperStart;
    private final float upperEnd;
    private final float upperRange;
    private final float upperExpandedRange;

    private final float compression;
    private final float compressionRange;

    private final Module module;

    public Compressor(Module module, float inset, float amount) {
        this(module, inset, inset + amount, 1 - inset - amount, 1 - inset);
    }

    public Compressor(Module module, float lowerStart, float lowerEnd, float upperStart, float upperEnd) {
        this.module = module;
        this.lowerStart = lowerStart;
        this.lowerEnd = lowerEnd;
        this.lowerRange = lowerStart;
        this.lowerExpandRange = lowerEnd;
        this.upperStart = upperStart;
        this.upperEnd = upperEnd;
        this.upperRange = 1 - upperEnd;
        this.upperExpandedRange = 1 - upperStart;
        this.compression = upperStart - lowerEnd;
        this.compressionRange = upperEnd - lowerStart;
    }

    @Override
    public float getValue(float x, float y) {
        float value = module.getValue(x, y);
        if (value <= lowerStart) {
            float alpha = value / lowerRange;
            return alpha * lowerExpandRange;
        } else if (value >= upperEnd) {
            float delta = value - upperEnd;
            float alpha = delta / upperRange;
            return upperStart + alpha * upperExpandedRange;
        } else {
            float delta = value - lowerStart;
            float alpha = delta / compressionRange;
            return lowerEnd + alpha * compression;
        }
    }
}
