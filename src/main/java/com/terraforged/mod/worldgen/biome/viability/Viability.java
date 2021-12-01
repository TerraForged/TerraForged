package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;

public interface Viability {
    Viability NONE = (x, z, data, generator) -> 1F;

    float getFitness(int x, int z, TerrainData data, Generator generator);

    default float getScaler(Generator generator) {
        return generator.getGenDepth() / 255F;
    }

    static float getFallOff(float value, float max) {
        return value < max ? 1F - (value / max) : 0F;
    }

    static float getFallOff(float value, float min, float mid, float max) {
        if (value < min) return 0F;
        if (value < mid) return (value - min) / (mid - min);
        if (value < max) return 1F - ((value - mid) / (max - mid));
        return 0F;
    }
}
