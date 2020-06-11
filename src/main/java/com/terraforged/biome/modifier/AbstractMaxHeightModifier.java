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

package com.terraforged.biome.modifier;

import com.terraforged.core.Seed;
import com.terraforged.core.cell.Cell;
import com.terraforged.world.climate.Climate;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import net.minecraft.world.biome.Biome;

public abstract class AbstractMaxHeightModifier extends AbstractOffsetModifier {

    private final float minHeight;
    private final float maxHeight;
    private final float range;
    private final Module variance;

    public AbstractMaxHeightModifier(Seed seed, Climate climate, int scale, int octaves, float variance, float minHeight, float maxHeight) {
        super(climate);
        this.minHeight = minHeight;
        this.maxHeight = maxHeight;
        this.range = maxHeight - minHeight;
        this.variance = Source.perlin(seed.next(), scale, octaves).scale(variance);
    }

    @Override
    protected final Biome modify(Biome in, Cell cell, int x, int z, float ox, float oz) {
        float var = variance.getValue(x, z);
        float value = cell.value + var;
        if (value < minHeight) {
            return in;
        }
        if (value > maxHeight) {
            return getModifiedBiome(in, cell, x, z, ox, oz);
        }
        float alpha = (value - minHeight) / range;
        cell.biomeEdge *= alpha;
        return in;
    }

    protected abstract Biome getModifiedBiome(Biome in, Cell cell, int x, int z, float ox, float oz);
}
