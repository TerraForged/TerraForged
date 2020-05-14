/*
 *
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

package com.terraforged.biome.provider;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.List;
import java.util.Random;
import java.util.Set;

public abstract class AbstractBiomeProvider extends net.minecraft.world.biome.provider.BiomeProvider {

    public AbstractBiomeProvider() {
        super(BiomeHelper.getAllBiomes());
    }

    @Override
    public final Set<Biome> func_225530_a_(int x, int y, int z, int size) {
        return getBiomesInSquare(x, y, z, size);
    }

    @Override
    public final BlockPos func_225531_a_(int centerX, int centerY, int centerZ, int range, List<Biome> biomes, Random random) {
        return findBiomePosition(centerX, centerY, centerZ, range, biomes, random);
    }

    public abstract Set<Biome> getBiomesInSquare(int x, int y, int z, int size);

    public abstract BlockPos findBiomePosition(int centerX, int centerY, int centerZ, int range, List<Biome> biomes, Random random);
}
