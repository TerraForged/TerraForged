package com.terraforged.mod.util.map;

import java.util.function.IntFunction;

public class ObjectMap<T> {
    private final Index index;
    private final T[] data;

    public ObjectMap(IntFunction<T[]> constructor) {
        index = Index.CHUNK;
        data = constructor.apply(16 * 16);
    }

    public ObjectMap(int border, IntFunction<T[]> constructor) {
        int size = 16 + border * 2;
        this.index = Index.borderedChunk(border);
        this.data = constructor.apply(size * size);
    }

    public Index getIndex() {
        return index;
    }

    public T get(int x, int z) {
        return get(index.of(x, z));
    }

    public void set(int x, int z, T value) {
        set(index.of(x, z), value);
    }

    public T get(int index) {
        return data[index];
    }

    public void set(int index, T value) {
        data[index] = value;
    }
}
