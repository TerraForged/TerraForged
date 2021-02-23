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

import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiomes;
import com.terraforged.engine.world.biome.map.defaults.FallbackBiomes;
import com.terraforged.mod.biome.ModBiomes;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class BiomeDefaults implements BiomeContext.Defaults<RegistryKey<Biome>> {

    private final BiomeContext<RegistryKey<Biome>> context;

    public BiomeDefaults(BiomeContext<RegistryKey<Biome>> context) {
        this.context = context;
    }

    @Override
    public DefaultBiomes getDefaults() {
        return new DefaultBiomes(
                TFDefaultBiomes.BEACH.create(context),
                TFDefaultBiomes.COAST.create(context),
                TFDefaultBiomes.RIVER.create(context),
                TFDefaultBiomes.LAKE.create(context),
                TFDefaultBiomes.WETLAND.create(context),
                TFDefaultBiomes.OCEAN.create(context),
                TFDefaultBiomes.DEEP_OCEAN.create(context),
                TFDefaultBiomes.MOUNTAIN.create(context),
                TFDefaultBiomes.VOLCANOES.create(context),
                TFDefaultBiomes.LAND.create(context)
        );
    }

    @Override
    public FallbackBiomes<RegistryKey<Biome>> getFallbacks() {
        return new FallbackBiomes<>(
                Biomes.RIVER,
                ModBiomes.LAKE,
                Biomes.BEACH,
                Biomes.OCEAN,
                Biomes.DEEP_OCEAN,
                Biomes.SWAMP,
                Biomes.PLAINS
        );
    }
}
