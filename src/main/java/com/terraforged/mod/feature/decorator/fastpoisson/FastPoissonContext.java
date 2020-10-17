package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.n2d.Module;

public class FastPoissonContext {

    public final int radius;
    public final int radius2;
    public final float frequency;
    public final Module density;

    public FastPoissonContext(int radius, float frequency, Module density) {
        this.radius2 = radius * radius;
        this.radius = radius;
        this.frequency = frequency;
        this.density = density;
    }
}
