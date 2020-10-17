package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.core.concurrent.cache.SafeCloseable;
import com.terraforged.mod.feature.decorator.poisson.BiomeVariance;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.util.NoiseUtil;

public class DensityNoise implements SafeCloseable, Module {

    private final BiomeVariance biome;
    private final Module variance;

    public DensityNoise(BiomeVariance biome, Module variance) {
        this.biome = biome;
        this.variance = variance;
    }

    @Override
    public float getValue(float x, float y) {
        float value1 = biome.getValue(x, y);
        if (value1 > 2F) {
            return value1;
        }

        float value2 = variance.getValue(x, y);
        if (value1 > 1F) {
            return NoiseUtil.lerp(value2, value1, (value1 - 0.25F) / 0.25F);
        }

        return value2;
    }

    @Override
    public void close() {
        biome.close();
    }
}
