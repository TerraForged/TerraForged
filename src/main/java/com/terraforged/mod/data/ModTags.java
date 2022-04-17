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

package com.terraforged.mod.data;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.Key;
import net.minecraft.world.level.biome.Biome;

public interface ModTags {
    Key.LazyTag<Biome> OVERWORLD = TerraForged.biomeTag("overworld");

    // Trees
    Key.LazyTag<Biome> COPSES = TerraForged.biomeTag("trees/copses");
    Key.LazyTag<Biome> HARDY = TerraForged.biomeTag("trees/hardy");
    Key.LazyTag<Biome> HARDY_SLOPES = TerraForged.biomeTag("trees/hardy_slopes");
    Key.LazyTag<Biome> PATCHY = TerraForged.biomeTag("trees/patchy");
    Key.LazyTag<Biome> RAINFOREST = TerraForged.biomeTag("trees/rainforest");
    Key.LazyTag<Biome> SPARSE = TerraForged.biomeTag("trees/sparse");
    Key.LazyTag<Biome> SPARSE_RAINFOREST = TerraForged.biomeTag("trees/sparse_rainforest");
    Key.LazyTag<Biome> TEMPERATE = TerraForged.biomeTag("trees/temperate");
}
