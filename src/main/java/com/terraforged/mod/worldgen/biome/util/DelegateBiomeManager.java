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

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class DelegateBiomeManager extends BiomeManager {
    protected BiomeManager delegate;

    public DelegateBiomeManager() {
        super(null, 0L);
    }

    protected void setDelegate(BiomeManager delegate) {
        this.delegate = delegate;
    }

    public BiomeManager withDifferentSource(BiomeManager.NoiseBiomeSource source) {
        return delegate.withDifferentSource(source);
    }

    public Biome getBiome(BlockPos pos) {
        return delegate.getBiome(pos);
    }

    public Biome getNoiseBiomeAtPosition(double x, double y, double z) {
        return delegate.getNoiseBiomeAtPosition(x, y, z);
    }

    public Biome getNoiseBiomeAtPosition(BlockPos pos) {
        return delegate.getNoiseBiomeAtPosition(pos);
    }

    public Biome getNoiseBiomeAtQuart(int x, int y, int z) {
        return delegate.getNoiseBiomeAtQuart(x, y, z);
    }
}
