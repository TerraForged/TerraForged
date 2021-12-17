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

package com.terraforged.mod.worldgen.biome.util;

import net.minecraft.world.level.biome.Biome;

import java.util.Arrays;

public class BiomeList {
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

        for (int i = 0; i < size; i++) {
            if (biomes[i] == biome) {
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
