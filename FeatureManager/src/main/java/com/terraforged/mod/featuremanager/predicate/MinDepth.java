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

package com.terraforged.mod.featuremanager.predicate;

import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

public class MinDepth implements FeaturePredicate {

    public static final MinDepth DEPTH60 = new MinDepth(60);
    public static final MinDepth DEPTH55 = new MinDepth(55);
    public static final MinDepth DEPTH45 = new MinDepth(45);

    private final int depth;

    public MinDepth(int depth) {
        this.depth = depth;
    }

    @Override
    public boolean test(IChunk chunk, Biome biome) {
        return chunk.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, 8, 8) < depth;
    }
}
