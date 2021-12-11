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

package com.terraforged.mod.worldgen.cave;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import net.minecraft.world.level.biome.Biome;

import java.util.Arrays;
import java.util.IdentityHashMap;
import java.util.Map;

public class CarverChunk {
    private Biome cached;
    private int cachedX, cachedZ;

    private int biomeListIndex = -1;
    private final BiomeList[] biomeLists;
    private final Map<NoiseCave, BiomeList> biomes = new IdentityHashMap<>();

    public CarverChunk(int size) {
        biomeLists = new BiomeList[size];
        for (int i = 0; i < biomeLists.length; i++) {
            biomeLists[i] = new BiomeList();
        }
    }

    public CarverChunk reset() {
        cached = null;
        biomes.clear();
        biomeListIndex = -1;
        return this;
    }

    public BiomeList getBiomes(NoiseCave config) {
        return biomes.get(config);
    }

    public Biome getBiome(int x, int z, NoiseCave config, Generator generator) {
        int biomeX = x >> 2;
        int biomeZ = z >> 2;
        if (cached == null || biomeX != cachedX || biomeZ != cachedZ) {
            cached = generator.getBiomeSource().getUnderGroundBiome(config.hashCode(), x, z, config.getType());
            cachedX = biomeX;
            cachedZ = biomeZ;
            biomes.computeIfAbsent(config, c -> nextList()).add(cached);
        }
        return cached;
    }

    private BiomeList nextList() {
        int i = biomeListIndex + 1;
        if (i < biomeLists.length) {
            biomeListIndex = i;
            return biomeLists[i].reset();
        }
        return new BiomeList();
    }

    public static class BiomeList {
        private int size = 0;
        private Biome[] biomes;

        public BiomeList reset() {
            size = 0;
            return this;
        }

        public int size() {
            return size;
        }

        public Biome get(int i ) {
            return biomes[i];
        }

        public boolean contains(Biome biome) {
            if (biomes == null) return false;

            for (var b : biomes) {
                if (b == biome) {
                    return true;
                }
            }

            return false;
        }

        public void add(Biome biome) {
            if (contains(biome)) return;

            grow(size + 1);
            biomes[size] = biome;
            size++;
        }

        private void grow(int size) {
            if (biomes == null) {
                biomes = new Biome[size];
            } else if (biomes.length <= size) {
                biomes = Arrays.copyOf(biomes, size);
            }
        }
    }
}
