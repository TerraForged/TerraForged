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

import com.terraforged.engine.world.biome.TempCategory;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.mod.biome.provider.BiomeHelper;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.function.Function;

public class BiomeProperties implements BiomeContext.Properties<RegistryKey<Biome>> {

    private final TFBiomeContext context;

    public BiomeProperties(TFBiomeContext context) {
        this.context = context;
    }

    @Override
    public BiomeContext<RegistryKey<Biome>> getContext() {
        return context;
    }

    @Override
    public float getDepth(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return biome.getDepth();
    }

    @Override
    public float getMoisture(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return biome.getDownfall();
    }

    @Override
    public float getTemperature(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return biome.getBaseTemperature();
    }

    @Override
    public TempCategory getTempCategory(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return BiomeHelper.getTempCategory(biome, context);
    }

    @Override
    public TempCategory getMountainCategory(RegistryKey<Biome> key) {
        Biome biome = context.biomes.get(key);
        return BiomeHelper.getMountainCategory(biome);
    }

    public <P> P getProperty(int id, Function<Biome, P> getter) {
        Biome biome = context.biomes.get(id);
        return getter.apply(biome);
    }
}
