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
