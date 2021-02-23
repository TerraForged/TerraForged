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

package com.terraforged.mod.biome.provider.analyser;

import com.terraforged.engine.world.biome.map.BiomeCollector;
import com.terraforged.mod.biome.context.TFBiomeContext;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class TFBiomeCollector implements BiomeCollector<RegistryKey<Biome>> {

    private final TFBiomeContext context;
    private final Set<Biome> biomes = new HashSet<>();

    public TFBiomeCollector(TFBiomeContext context) {
        this.context = context;
    }

    @Override
    public TFBiomeCollector add(RegistryKey<Biome> key) {
        biomes.add(context.biomes.get(key));
        return this;
    }

    public Biome[] getBiomes() {
        Biome[] biomes = this.biomes.toArray(new Biome[0]);
        Arrays.sort(biomes, Comparator.comparing(context.biomes::getRegistryName));
        return biomes;
    }
}
