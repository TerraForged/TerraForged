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

package com.terraforged.mod.util;

import com.terraforged.noise.util.NoiseUtil;

public class MathUtil {
    public static final float EPSILON = 0.99999F;

    public static int hash(int seed, int x) {
        return NoiseUtil.hash(seed, x);
    }

    public static int hash(int seed, int x, int z) {
        return NoiseUtil.hash2D(seed, x, z);
    }

    public static float randX(int hash) {
        return rand(hash, NoiseUtil.X_PRIME);
    }

    public static float randZ(int hash) {
        return rand(hash, NoiseUtil.Y_PRIME);
    }

    public static float rand(int seed, int offset) {
        return rand(hash(seed, offset));
    }

    public static float rand(int n) {
        n ^= 1619;
        n ^= 31337;
        float value = (n * n * n * '\uec4d') / 2.14748365E9F;
        return NoiseUtil.map(value, -1, 1, 2);
    }

    public static float sum(float[] values) {
        float sum = 0F;
        for (float value : values) {
            sum += value;
        }
        return sum;
    }
}
