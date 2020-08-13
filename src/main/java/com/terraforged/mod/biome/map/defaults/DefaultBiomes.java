package com.terraforged.mod.biome.map.defaults;

import com.terraforged.core.cell.Cell;
import com.terraforged.mod.biome.ModBiomes;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class DefaultBiomes {

    public static final DefaultBiome.Factory NONE = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.THE_VOID),
            context.biomes.get(Biomes.THE_VOID),
            context.biomes.get(Biomes.THE_VOID),
            0.25F,
            0.75F
    );

    public static final DefaultBiome.Factory BEACH = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.SNOWY_BEACH),
            context.biomes.get(Biomes.BEACH),
            context.biomes.get(ModBiomes.WARM_BEACH),
            0.25F,
            0.75F
    );

    public static final DefaultBiome.Factory RIVER = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.FROZEN_RIVER),
            context.biomes.get(Biomes.RIVER),
            context.biomes.get(Biomes.RIVER),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory LAKE = context -> new DefaultBiomeSelector(
            context.biomes.get(ModBiomes.FROZEN_LAKE),
            context.biomes.get(ModBiomes.LAKE),
            context.biomes.get(ModBiomes.LAKE),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory WETLAND = context -> new DefaultBiomeSelector(
            context.biomes.get(ModBiomes.COLD_MARSHLAND),
            context.biomes.get(Biomes.SWAMP),
            context.biomes.get(ModBiomes.MARSHLAND),
            0.4F,
            1F
    ) {
        @Override
        public Biome getDefaultBiome(Cell cell) {
            if (cell.temperature > BiomeTemps.COLD) {
                if (cell.moisture > 0.65F) {
                    return warm;
                }
                if (cell.moisture > 0.4F) {
                    return medium;
                }
            }
            return getNone();
        };
    };

    public static final DefaultBiome.Factory OCEAN = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.FROZEN_OCEAN),
            context.biomes.get(Biomes.OCEAN),
            context.biomes.get(Biomes.WARM_OCEAN),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory DEEP_OCEAN = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.DEEP_FROZEN_OCEAN),
            context.biomes.get(Biomes.DEEP_OCEAN),
            context.biomes.get(Biomes.DEEP_WARM_OCEAN),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory MOUNTAIN = context -> new DefaultBiomeSelector(
            context.biomes.get(Biomes.SNOWY_MOUNTAINS),
            context.biomes.get(Biomes.THE_VOID),
            context.biomes.get(Biomes.THE_VOID),
            0.25F,
            1F
    );

    public static final DefaultBiome.Factory LAND = context -> new DefaultBiomeSelector(
            context.biomes.get(ModBiomes.TAIGA_SCRUB),
            context.biomes.get(Biomes.PLAINS),
            context.biomes.get(ModBiomes.SAVANNA_SCRUB),
            0.3F,
            1.7F
    );

    public static boolean overridesRiver(Biome biome) {
        return biome.getCategory() == Biome.Category.SWAMP || biome.getCategory() == Biome.Category.JUNGLE;
    }
}
