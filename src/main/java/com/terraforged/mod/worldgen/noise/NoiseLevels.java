package com.terraforged.mod.worldgen.noise;

import com.terraforged.mod.worldgen.terrain.TerrainLevels;

public class NoiseLevels {
    public final float depthMin;
    public final float depthRange;

    public final float heightMin;
    public final float heightRange;

    public final float frequency;

    public NoiseLevels(int seaLevel, int genDepth) {
        this.depthMin = (seaLevel - 40) / (float) genDepth;
        this.heightMin = seaLevel / (float) genDepth;
        this.heightRange = 1F - heightMin;
        this.depthRange = heightMin - depthMin;
        this.frequency = calcFrequency(genDepth - seaLevel);
    }

    public float toDepthNoise(float noise) {
        return depthMin + noise * depthRange;
    }

    public float toHeightNoise(float noise) {
        return heightMin + noise * heightRange;
    }

    public static NoiseLevels getDefault() {
        return TerrainLevels.DEFAULT.noiseLevels;
    }

    public static float calcFrequency(int verticalRange) {
        return (TerrainLevels.DEFAULT_GEN_DEPTH - TerrainLevels.DEFAULT_SEA_LEVEL) / (float) verticalRange;
    }
}
