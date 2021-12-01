package com.terraforged.mod.worldgen.biome;

import com.terraforged.mod.worldgen.biome.decorator.FeatureDecorator;
import com.terraforged.mod.worldgen.biome.decorator.SurfaceDecorator;
import net.minecraft.core.RegistryAccess;

public class BiomeGenerator {
    private final SurfaceDecorator surfaceDecorator;
    private final FeatureDecorator featureDecorator;

    public BiomeGenerator(long seed, RegistryAccess access) {
        this.surfaceDecorator = new SurfaceDecorator();
        this.featureDecorator = new FeatureDecorator(access);
    }

    public SurfaceDecorator getSurfaceDecorator() {
        return surfaceDecorator;
    }

    public FeatureDecorator getFeatureDecorator() {
        return featureDecorator;
    }
}
