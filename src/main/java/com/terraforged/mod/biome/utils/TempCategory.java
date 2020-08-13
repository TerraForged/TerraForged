package com.terraforged.mod.biome.utils;

import com.terraforged.fm.GameContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.feature.structure.OceanRuinConfig;
import net.minecraft.world.gen.feature.structure.OceanRuinStructure;

import java.util.Optional;

public enum TempCategory {
    COLD,
    MEDIUM,
    WARM,
    ;

    public static TempCategory forBiome(Biome biome, GameContext context) {
        // vanilla ocean biome properties are not at all helpful for determining temperature
        if (biome.getCategory() == Biome.Category.OCEAN) {
            // warm & luke_warm oceans get OceanRuinStructure.Type.WARM
            Optional<OceanRuinConfig> config = Structures.getConfig(biome, Structures.oceanRuin(), OceanRuinConfig.class);
            if (config.isPresent()) {
                if (config.get().field_204031_a == OceanRuinStructure.Type.WARM) {
                    return TempCategory.WARM;
                }

                if (config.get().field_204031_a == OceanRuinStructure.Type.COLD) {
                    return TempCategory.COLD;
                }
            }

            // if the id contains the world cold or frozen, assume it's cold
            if (context.biomes.getName(biome).contains("cold") || context.biomes.getName(biome).contains("frozen")) {
                return TempCategory.COLD;
            }

            // the rest we categorize as medium
            return TempCategory.MEDIUM;
        }
        // hopefully biomes otherwise have a sensible category
        return TempCategory.MEDIUM;
    }
}
