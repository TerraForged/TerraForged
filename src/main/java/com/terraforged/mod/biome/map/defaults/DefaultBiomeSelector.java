package com.terraforged.mod.biome.map.defaults;

import net.minecraft.world.biome.Biome;

public class DefaultBiomeSelector implements DefaultBiome {

    protected final float lower;
    protected final float upper;
    protected final Biome cold;
    protected final Biome medium;
    protected final Biome warm;

    public DefaultBiomeSelector(Biome cold, Biome medium, Biome warm, float lower, float upper) {
        this.cold = cold;
        this.medium = medium;
        this.warm = warm;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public Biome getBiome(float temperature) {
        if (temperature < lower) {
            return cold;
        }
        if (temperature > upper) {
            return warm;
        }
        return medium;
    }
}
