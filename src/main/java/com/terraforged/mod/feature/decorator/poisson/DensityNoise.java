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

package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.engine.concurrent.cache.SafeCloseable;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

public class DensityNoise implements SafeCloseable, Module {

    private final BiomeVariance biome;
    private final Module variance;

    public DensityNoise(BiomeVariance biome, Module variance) {
        this.biome = biome;
        this.variance = variance;
    }

    @Override
    public float getValue(float x, float y) {
        float value1 = biome.getValue(x, y);
        if (value1 > 2F) {
            return value1;
        }

        float value2 = variance.getValue(x, y);
        if (value1 > 1F) {
            return NoiseUtil.lerp(value2, value1, (value1 - 0.25F) / 0.25F);
        }

        return value2;
    }

    @Override
    public void close() {
        biome.close();
    }
}
