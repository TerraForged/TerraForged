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

        return values[0];
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
