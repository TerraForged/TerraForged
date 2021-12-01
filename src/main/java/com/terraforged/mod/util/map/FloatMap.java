package com.terraforged.mod.util.map;

public class FloatMap {
    private final Index index;
    private final float[] data;

    public FloatMap() {
        index = Index.CHUNK;
        data = new float[16 * 16];
    }

    public FloatMap(int border) {
        int size = 16 + border * 2;
        this.index = Index.borderedChunk(border);
        this.data = new float[size * size];
    }

    public Index index() {
        return index;
    }

    public float get(int x, int z) {
        return get(index.of(x, z));
    }

    public void set(int x, int z, float value) {
        set(index.of(x, z), value);
    }

    public float get(int index) {
        return data[index];
    }

    public void set(int index, float value) {
        data[index] = value;
    }
}
