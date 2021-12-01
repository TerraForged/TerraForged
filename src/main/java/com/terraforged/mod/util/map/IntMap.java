package com.terraforged.mod.util.map;

public class IntMap {
    private final Index index;
    private final int[] data;

    private int max = 0;

    public IntMap() {
        index = Index.CHUNK;
        data = new int[16 * 16];
    }

    public IntMap(int border) {
        int size = 16 + border * 2;
        this.index = Index.borderedChunk(border);
        this.data = new int[size * size];
    }

    public Index getIndex() {
        return index;
    }

    public int getMax() {
        return max;
    }

    public int get(int x, int z) {
        return get(index.of(x, z));
    }

    public void set(int x, int z, int value) {
        set(index.of(x, z), value);
    }

    public int get(int index) {
        return data[index];
    }

    public void set(int index, int value) {
        data[index] = value;
        max = Math.max(value, max);
    }
}
