package com.terraforged.core.world.geology;

import me.dags.noise.Module;
import me.dags.noise.Source;

public class Stratum<T> {

    private final T value;
    private final Module depth;

    public Stratum(T value, double depth) {
        this(value, Source.constant(depth));
    }

    public Stratum(T value, Module depth) {
        this.depth = depth;
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public float getDepth(float x, float z) {
        return depth.getValue(x, z);
    }

    public static <T> Stratum<T> of(T t, double depth) {
        return new Stratum<>(t, depth);
    }

    public static <T> Stratum<T> of(T t, Module depth) {
        return new Stratum<>(t, depth);
    }

    public interface Visitor<T> {

        boolean visit(int y, T value);
    }
}
