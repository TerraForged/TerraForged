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
            context.biomes.get(ModBiomes.MARSHLAND),
            context.biomes.get(Biomes.SWAMP),
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
