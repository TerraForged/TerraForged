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

package com.terraforged.mod.worldgen.biome.viability;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.terrain.TerrainData;

public interface Viability {
    Viability NONE = (x, z, data, generator) -> 1F;

    float getFitness(int x, int z, TerrainData data, Generator generator);

    default float getScaler(Generator generator) {
        return generator.getGenDepth() / 255F;
    }

    static float getFallOff(float value, float max) {
        return value < max ? 1F - (value / max) : 0F;
    }

    static float getFallOff(float value, float min, float mid, float max) {
        if (value < min) return 0F;
        if (value < mid) return (value - min) / (mid - min);
        if (value < max) return 1F - ((value - mid) / (max - mid));
        return 0F;
    }
}
