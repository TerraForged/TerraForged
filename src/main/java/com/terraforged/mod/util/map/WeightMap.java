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

package com.terraforged.mod.util.map;

import com.terraforged.mod.util.MathUtil;

public class WeightMap<T> {
    protected final T[] values;
    protected final float[] weights;
    protected final float sumWeight;
    protected final float zeroWeight;

    public WeightMap(T[] values, float[] weights) {
        this.values = values;
        this.weights = getCumulativeWeights(values.length, weights);
        this.zeroWeight = weights.length > 0 ? weights[0] : 0;
        this.sumWeight = MathUtil.sum(weights) * MathUtil.EPSILON;
    }

    public T[] getValues() {
        return values;
    }

    public T getValue(float noise) {
        noise *= sumWeight;

        if (noise < zeroWeight) return values[0];

        for (int i = 1; i < weights.length; i++) {
            if (noise < weights[i]) {
                return values[i];
            }
        }

        return null;
    }

    public interface Weighted {
        float weight();
    }

    public static <T extends Weighted> WeightMap<T> of(T[] values) {
        float[] weights = new float[values.length];
        for (int i = 0; i < weights.length; i++) {
            weights[i] = values[i].weight();
        }
        return new WeightMap<>(values, weights);
    }

    private static float[] getCumulativeWeights(int len, float[] weights) {
        float[] cumulativeWeights = new float[len];

        float weight = 0F;
        for (int i = 0; i < len; i++) {
            weight += i < weights.length ? weights[i] : 1;
            cumulativeWeights[i] = weight;
        }

        return cumulativeWeights;
    }
}
