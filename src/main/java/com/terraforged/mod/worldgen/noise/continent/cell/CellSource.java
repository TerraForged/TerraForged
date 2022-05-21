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

package com.terraforged.mod.worldgen.noise.continent.cell;

import com.terraforged.noise.func.Interpolation;
import com.terraforged.noise.util.Noise;

public enum CellSource {
    PERLIN {
        @Override
        public float sample(int seed, float x, float y) {
            return Noise.singlePerlin2(x, y, seed, Interpolation.CURVE3);
        }
    },
    SIMPLEX {
        @Override
        public float sample(int seed, float x, float y) {
            return Noise.singleSimplex(x, y, seed);
        }
    },
    CUBIC {
        @Override
        public float sample(int seed, float x, float y) {
            return Noise.singleCubic(x, y, seed);
        }
    },
    ;

    public abstract float sample(int seed, float x, float y);

    public float getValue(int seed, float x, float y) {
        return (1 + sample(seed, x, y)) * 0.5f;
    }
}