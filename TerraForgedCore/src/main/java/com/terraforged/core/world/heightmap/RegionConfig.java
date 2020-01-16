package com.terraforged.core.world.heightmap;

import me.dags.noise.Module;

public class RegionConfig {

    public final int seed;
    public final int scale;
    public final Module warpX;
    public final Module warpZ;
    public final double warpStrength;

    public RegionConfig(int seed, int scale, Module warpX, Module warpZ, double warpStrength) {
        this.seed = seed;
        this.scale = scale;
        this.warpX = warpX;
        this.warpZ = warpZ;
        this.warpStrength = warpStrength;
    }
}
