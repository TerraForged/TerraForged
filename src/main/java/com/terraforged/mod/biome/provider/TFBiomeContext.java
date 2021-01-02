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

package com.terraforged.mod.biome.provider;

import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.engine.world.biome.map.defaults.DefaultBiomes;
import com.terraforged.engine.world.biome.map.defaults.FallbackBiomes;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.featuremanager.GameContext;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

public class TFBiomeContext implements BiomeContext<RegistryKey<Biome>> {

    private final GameContext context;
    private final DefaultBiomes defaultBiomes;
    private final FallbackBiomes<RegistryKey<Biome>> fallbackBiomes;

    public TFBiomeContext(GameContext context) {
        this.context = context;
        this.defaultBiomes = new DefaultBiomes(
                TFDefaultBiomes.BEACH.create(this),
                TFDefaultBiomes.RIVER.create(this),
                TFDefaultBiomes.LAKE.create(this),
                TFDefaultBiomes.WETLAND.create(this),
                TFDefaultBiomes.OCEAN.create(this),
                TFDefaultBiomes.DEEP_OCEAN.create(this),
                TFDefaultBiomes.MOUNTAIN.create(this),
                TFDefaultBiomes.VOLCANOES.create(this),
                TFDefaultBiomes.LAND.create(this)
        );
        this.fallbackBiomes = new FallbackBiomes<>(
                Biomes.RIVER,
                ModBiomes.LAKE,
                Biomes.STONE_SHORE,
                Biomes.BEACH,
                Biomes.OCEAN,
                Biomes.DEEP_OCEAN,
                Biomes.SWAMP,
                Biomes.PLAINS
        );
    }

    @Override
    public int getId(RegistryKey<Biome> key) {
        return context.biomes.getId(key);
    }

    @Override
    public RegistryKey<Biome> getValue(int id) {
        return context.biomes.getKey(context.biomes.get(id));
    }

    @Override
    public String getName(int id) {
        return context.biomes.getName(id);
    }

    @Override
    public IntSet getRiverOverrides() {
        IntSet set = new IntOpenHashSet();
        for (Biome biome : context.biomes) {
            if (TFDefaultBiomes.overridesRiver(biome)) {
                set.add(context.biomes.getId(biome));
            }
        }
        return set;
    }

    @Override
    public DefaultBiomes getDefaults() {
        return defaultBiomes;
    }

    @Override
    public FallbackBiomes<RegistryKey<Biome>> getFallbacks() {
        return fallbackBiomes;
    }

    @Override
    public float getDepth(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return biome.getDepth();
    }

    @Override
    public TempCategory getTempCategory(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return BiomeHelper.getTempCategory(biome, context);
    }

    @Override
    public TempCategory getMountainCategory(RegistryKey<Biome> key) {
        return BiomeHelper.getMountainCategory(context.biomes.get(key));
    }
}
