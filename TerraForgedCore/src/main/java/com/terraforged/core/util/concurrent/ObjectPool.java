package com.terraforged.core.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {

    private final int capacity;
    private final List<Item<T>> pool;
    private final Supplier<? extends T> supplier;

    public ObjectPool(int size, Supplier<? extends T> supplier) {
        this.capacity = size;
        this.pool = new ArrayList<>(size);
        this.supplier = supplier;
    }

    public Item<T> get() {
        synchronized (pool) {
            if (pool.size() > 0) {
                return pool.remove(pool.size() - 1).retain();
            }
        }
        return new Item<>(supplier.get(), this);
    }

    public int size() {
        synchronized (pool) {
            return pool.size();
        }
    }

    private boolean restore(Item<T> item) {
        synchronized (pool) {
            int size = pool.size();
            if (size < capacity) {
                pool.add(item);
                return true;
            }
        }
        return false;
    }

    public static class Item<T> implements AutoCloseable {

        private final T value;
        private final ObjectPool<T> pool;

        private boolean released = false;

        private Item(T value, ObjectPool<T> pool) {
            this.value = value;
            this.pool = pool;
        }

        public T getValue() {
            return value;
        }

        public void release() {
            if (!released) {
                released = true;
                released = pool.restore(this);
            }
        }

        private Item<T> retain() {
            released = false;
            return this;
        }

        @Override
        public void close() {
            release();
        }
    }
}
