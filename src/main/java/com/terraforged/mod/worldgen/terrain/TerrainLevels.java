package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.noise.util.NoiseUtil;

public class TerrainLevels {
    public static final int DEFAULT_SEA_LEVEL = 63;
    public static final int DEFAULT_GEN_DEPTH = 255;
    public static final TerrainLevels DEFAULT = new TerrainLevels(DEFAULT_SEA_LEVEL, DEFAULT_GEN_DEPTH);

    public final int seaLevel;
    public final int genDepth;
    public final NoiseLevels noiseLevels;

    public TerrainLevels() {
        this(DEFAULT_SEA_LEVEL, 384);
    }

    public TerrainLevels(int seaLevel, int genDepth) {
        this.seaLevel = seaLevel;
        this.genDepth = genDepth;
        this.noiseLevels = new NoiseLevels(seaLevel, genDepth);
    }

    public float getScaledHeight(float heightNoise) {
        return heightNoise * genDepth;
    }

    public int getHeight(float scaledHeight) {
        return NoiseUtil.floor(scaledHeight);
    }
}
