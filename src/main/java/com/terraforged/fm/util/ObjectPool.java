/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.fm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ObjectPool<T> {

    private final int capacity;
    private final List<Item<T>> pool;
    private final Object lock = new Object();
    private final Supplier<? extends T> supplier;

    public ObjectPool(int size, Supplier<? extends T> supplier) {
        this.capacity = size;
        this.pool = new ArrayList<>(size);
        this.supplier = supplier;
    }

    public Item<T> get() {
        synchronized (lock) {
            if (pool.size() > 0) {
                return pool.remove(pool.size() - 1).retain();
            }
        }
        return new Item<>(supplier.get(), this);
    }

    private boolean restore(Item<T> item) {
        synchronized (lock) {
            if (pool.size() < capacity) {
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
