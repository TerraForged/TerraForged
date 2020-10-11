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

package com.terraforged.fm.predicate;

import com.terraforged.fm.GameContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.IChunk;

import java.util.HashSet;
import java.util.Set;

public class BiomePredicate implements FeaturePredicate {

    private final Set<Biome> biomes;

    private BiomePredicate(Set<Biome> biomes) {
        this.biomes = biomes;
    }

    @Override
    public boolean test(IChunk chunk, Biome biome) {
        BiomeContainer biomes = chunk.getBiomes();
        if (biomes == null) {
            return false;
        }
        for (int dz = 4; dz < 16; dz += 8) {
            for (int dx = 4; dx < 16; dx += 8) {
                Biome b = biomes.getNoiseBiome(dx, 0, dz);
                if (!this.biomes.contains(b)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static BiomePredicate oceans(GameContext context) {
        return BiomePredicate.of(context, Biome.Category.OCEAN);
    }

    public static BiomePredicate of(GameContext context, Biome.Category... categories) {
        Set<Biome> set = new HashSet<>();
        for (Biome biome : context.biomes) {
            for (Biome.Category category : categories) {
                if (biome.getCategory() == category) {
                    set.add(biome);
                    break;
                }
            }
        }
        return new BiomePredicate(set);
    }
}
