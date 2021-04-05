/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.biome.provider;

import com.terraforged.engine.settings.ClimateSettings;
import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.defaults.BiomeTemps;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeGenerationSettings;
import net.minecraft.world.gen.surfacebuilders.ConfiguredSurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.ISurfaceBuilderConfig;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraftforge.common.BiomeDictionary;

import javax.annotation.Nullable;

public class BiomeHelper {

    private static final String[] COLD_KEYWORDS = {"cold", "frozen", "ice", "chill", "tundra", "taiga", "arctic"};
    private static final String[] WARM_KEYWORDS = {"hot", "warm", "tropic", "desert", "savanna", "jungle"};

    public static TempCategory getTempCategory(Biome biome, TFBiomeContext context) {
        // vanilla ocean biome properties are not at all helpful for determining temperature
        if (biome.getBiomeCategory() == Biome.Category.OCEAN) {
            return tempFromName(context.biomes.getName(biome));
        } else if (biome.getBiomeCategory() == Biome.Category.BEACH) {
            // Beaches have slightly modified temps
            float temp = biome.getBaseTemperature();
            if (temp <= 0.4F) {
                return TempCategory.COLD;
            }
            if (temp >= 1.0) {
                return TempCategory.WARM;
            }
            return tempFromName(context.biomes.getName(biome));
        } else {
            // snowy = 0.0F, plains = 0.8F, desert = 2.0F
            float temp = biome.getBaseTemperature();
            if (temp <= 0.3F) {
                return TempCategory.COLD;
            }
            if (temp > 0.8) {
                return TempCategory.WARM;
            }
            // hopefully biomes otherwise have a sensible category
            return tempFromName(context.biomes.getName(biome));
        }
    }

    public static TempCategory tempFromName(String name) {
        if (containsKeyword(name, COLD_KEYWORDS)) {
            return TempCategory.COLD;
        }
        if (containsKeyword(name, WARM_KEYWORDS)) {
            return TempCategory.WARM;
        }
        return TempCategory.MEDIUM;
    }

    public static boolean containsKeyword(String name, String[] keywords) {
        for (String keyword : keywords) {
            if (name.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static float getBiomeSizeSearchModifier(ClimateSettings settings) {
        float defaultSize = ClimateSettings.BiomeShape.DEFAULT_BIOME_SIZE;
        float scale = settings.biomeShape.biomeSize / defaultSize;
        return NoiseUtil.clamp(scale * 0.75F, 0F, 1.25F);
    }

    public static TempCategory getMountainCategory(Biome biome) {
        if (getDefaultTemperature(biome) <= BiomeTemps.COLD) {
            return TempCategory.COLD;
        }
        if (getDefaultTemperature(biome) >= BiomeTemps.HOT) {
            return TempCategory.WARM;
        }
        return TempCategory.MEDIUM;
    }

    public static float getDefaultTemperature(Biome biome) {
        return biome.getBaseTemperature();
    }

    public static ISurfaceBuilderConfig getSurface(Biome biome) {
        if (biome != null) {
            if (biome.getGenerationSettings() != null && biome.getGenerationSettings().getSurfaceBuilderConfig() != null) {
                return biome.getGenerationSettings().getSurfaceBuilderConfig();
            }
        }
        return SurfaceBuilder.CONFIG_COARSE_DIRT;
    }

    public static ConfiguredSurfaceBuilder<?> getSurfaceBuilder(Biome biome) {
        if (biome != null) {
            if (biome.getGenerationSettings() != null && biome.getGenerationSettings().getSurfaceBuilder() != null) {
                return biome.getGenerationSettings().getSurfaceBuilder().get();
            }
        }
        return SurfaceBuilder.DEFAULT.configured(SurfaceBuilder.CONFIG_GRASS);
    }

    public static BiomeGenerationSettings getGenSettings(Biome biome) {
        return biome.getGenerationSettings();
    }

    public static boolean isOverworldBiome(Biome biome, TFBiomeContext context) {
        return isOverworldBiome(context.biomes.getKey(biome));
    }

    public static boolean isOverworldBiome(@Nullable RegistryKey<Biome> key) {
        if (key == null) {
            return false;
        }
        ResourceLocation name = key.location();
        if (name.getNamespace().equals(TerraForgedMod.MODID)) {
            return true;
        }
        return BiomeDictionary.getTypes(key).contains(BiomeDictionary.Type.OVERWORLD);
    }
}
