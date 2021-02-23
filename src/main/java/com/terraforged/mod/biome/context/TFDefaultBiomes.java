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

package com.terraforged.mod.biome.context;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.map.BiomeMap;
import com.terraforged.engine.world.biome.map.defaults.BiomeTemps;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiome;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiomeSelector;
import com.terraforged.mod.biome.ModBiomes;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class TFDefaultBiomes {

    public static final DefaultBiome.Factory<RegistryKey<Biome>> BEACH = context -> new DefaultBiomeSelector(
            context.getId(Biomes.SNOWY_BEACH),
            context.getId(Biomes.BEACH),
            context.getId(ModBiomes.WARM_BEACH),
            0.25F,
            0.75F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> COAST = context -> new DefaultBiomeSelector(
            BiomeMap.NULL_BIOME,
            BiomeMap.NULL_BIOME,
            BiomeMap.NULL_BIOME,
            0.25F,
            0.75F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> RIVER = context -> new DefaultBiomeSelector(
            context.getId(Biomes.FROZEN_RIVER),
            context.getId(Biomes.RIVER),
            context.getId(Biomes.RIVER),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> LAKE = context -> new DefaultBiomeSelector(
            context.getId(ModBiomes.FROZEN_LAKE),
            context.getId(ModBiomes.LAKE),
            context.getId(ModBiomes.LAKE),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> WETLAND = context -> new DefaultBiomeSelector(
            context.getId(ModBiomes.COLD_MARSHLAND),
            context.getId(ModBiomes.MARSHLAND),
            context.getId(Biomes.SWAMP),
            0.4F,
            1F
    ) {
        @Override
        public int getDefaultBiome(Cell cell) {
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

    public static final DefaultBiome.Factory<RegistryKey<Biome>> OCEAN = context -> new DefaultBiomeSelector(
            context.getId(Biomes.FROZEN_OCEAN),
            context.getId(Biomes.OCEAN),
            context.getId(Biomes.WARM_OCEAN),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> DEEP_OCEAN = context -> new DefaultBiomeSelector(
            context.getId(Biomes.DEEP_FROZEN_OCEAN),
            context.getId(Biomes.DEEP_OCEAN),
            context.getId(Biomes.DEEP_WARM_OCEAN),
            0.15F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> MOUNTAIN = context -> new DefaultBiomeSelector(
            context.getId(Biomes.SNOWY_MOUNTAINS),
            BiomeMap.NULL_BIOME,
            BiomeMap.NULL_BIOME,
            0.25F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> VOLCANOES = context -> new DefaultBiomeSelector(
            BiomeMap.NULL_BIOME,
            BiomeMap.NULL_BIOME,
            BiomeMap.NULL_BIOME,
            0.25F,
            1F
    );

    public static final DefaultBiome.Factory<RegistryKey<Biome>> LAND = context -> new DefaultBiomeSelector(
            context.getId(ModBiomes.TAIGA_SCRUB),
            context.getId(Biomes.PLAINS),
            context.getId(ModBiomes.SAVANNA_SCRUB),
            0.3F,
            1.7F
    );

    public static boolean overridesRiver(Biome biome) {
        return biome.getCategory() == Biome.Category.SWAMP || biome.getCategory() == Biome.Category.JUNGLE;
    }
}
