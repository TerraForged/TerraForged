package com.terraforged.core.util;

public class Seed {

    private final Seed root;

    private final int value;
    private int seed;

    public Seed(long seed) {
        this((int) seed);
    }

    public Seed(int seed) {
        this.value = seed;
        this.seed = seed;
        this.root = this;
    }

    private Seed(Seed root) {
        this.root = root;
        this.seed = root.next();
        this.value = seed;
    }

    public int next() {
        return ++root.seed;
    }

    public int get() {
        return value;
    }

    public Seed nextSeed() {
        return new Seed(root);
    }

    public Seed reset() {
        this.seed = value;
        return this;
    }
}
