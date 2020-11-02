/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

        // snowy = 0.0F
        // plains = 0.8F
        // desert = 2.0F
        float temp = biome.getTemperature();
        if (temp <= 0.3F) {
            return TempCategory.COLD;
        }
        if (temp > 0.8) {
            return TempCategory.WARM;
        }
        // hopefully biomes otherwise have a sensible category
        return TempCategory.MEDIUM;
    }
}
