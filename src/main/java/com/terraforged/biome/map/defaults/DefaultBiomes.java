package com.terraforged.biome.map.defaults;

import com.terraforged.biome.ModBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class DefaultBiomes {

    public static Biome defaultBeach(float temperature) {
        if (temperature < 0.25) {
            return Biomes.SNOWY_BEACH;
        }
        if (temperature > 0.75) {
            return ModBiomes.WARM_BEACH;
        }
        return Biomes.BEACH;
    }

    public static Biome defaultRiver(float temperature) {
        if (temperature < 0.15) {
            return Biomes.FROZEN_RIVER;
        }
        return Biomes.RIVER;
    }

    public static Biome defaultLake(float temperature) {
        if (temperature < 0.15) {
            return ModBiomes.FROZEN_LAKE;
        }
        return ModBiomes.LAKE;
    }

    public static Biome defaultWetland(float temperature) {
        if (temperature < 0.15) {
            return ModBiomes.TAIGA_SCRUB;
        }
        return ModBiomes.MARSHLAND;
    }

    public static Biome defaultOcean(float temperature) {
        if (temperature < 0.3) {
            return Biomes.FROZEN_OCEAN;
        }
        if (temperature > 0.7) {
            return Biomes.WARM_OCEAN;
        }
        return Biomes.OCEAN;
    }

    public static Biome defaultDeepOcean(float temperature) {
        if (temperature < 0.3) {
            return Biomes.DEEP_FROZEN_OCEAN;
        }
        if (temperature > 0.7) {
            return Biomes.DEEP_WARM_OCEAN;
        }
        return Biomes.DEEP_OCEAN;
    }

    public static Biome defaultBiome(float temperature) {
        if (temperature < 0.3) {
            return ModBiomes.TAIGA_SCRUB;
        }
        if (temperature > 0.7) {
            return ModBiomes.SAVANNA_SCRUB;
        }
        return Biomes.PLAINS;
    }

    public static boolean overridesRiver(Biome biome) {
        return biome.getCategory() == Biome.Category.SWAMP || biome.getCategory() == Biome.Category.JUNGLE;
    }
}
