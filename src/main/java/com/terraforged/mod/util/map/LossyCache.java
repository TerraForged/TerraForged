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

import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class LossyCache<T> {
    protected final long[] keys;
    protected final T[] values;
    protected final int mask;
    protected final Consumer<T> removalListener;

    private LossyCache(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        capacity = Mth.smallestEncompassingPowerOfTwo(capacity);
        this.mask = capacity - 1;
        this.keys = new long[capacity];
        this.values = constructor.apply(capacity);
        this.removalListener = removalListener;
        Arrays.fill(this.keys, Long.MIN_VALUE);
    }

    public T computeIfAbsent(long key, Long2ObjectFunction<T> function) {
        int hash = hash(key);
        int index = hash & mask;
        T value = values[index];

        if (keys[index] == key && value != null) return value;

        T newValue = function.apply(key);
        keys[index] = key;
        values[index] = newValue;

        onRemove(value);

        return newValue;
    }

    protected void onRemove(T value) {
        if (value != null) {
            removalListener.accept(value);
        }
    }

    private static int hash(long l) {
        return (int) HashCommon.mix(l);
    }

    public static <T> LossyCache<T> of(int capacity, IntFunction<T[]> constructor) {
        return of(capacity, constructor, t -> {});
    }

    public static <T> LossyCache<T> of(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        return new LossyCache<>(capacity, constructor, removalListener);
    }

    public static <T> LossyCache<T> concurrent(int capacity, IntFunction<T[]> constructor) {
        return concurrent(capacity, constructor, t -> {});
    }

    public static <T> LossyCache<T> concurrent(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        return new LossyCache.Concurrent<>(capacity, constructor, removalListener);
    }

    public static class Concurrent<T> extends LossyCache<T> {
        protected final StampedLock lock = new StampedLock();

        public Concurrent(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
            super(capacity, constructor, removalListener);
        }

        @Override
        public T computeIfAbsent(long key, Long2ObjectFunction<T> function) {
            int hash = hash(key);
            int index = hash & mask;

            // Try reading without locking
            long optStamp = lock.tryOptimisticRead();
            long currentKey = keys[index];
            T currentValue = values[index];

            if (lock.validate(optStamp)) {
                // Safely read so check if the keys match and the value is non-null
                if (currentKey == key && currentValue != null) {
                    return currentValue;
                }
                // Otherwise we need to overwrite the current key/value
            } else {
                // A write occurred during the optimistic read so obtain a full read lock
                long read = lock.readLock();

                try {
                    currentKey = keys[index];
                    currentValue = values[index];
                } finally {
                    lock.unlockRead(read);
                }

                // Keys match and value is non-null
                if (currentKey == key && currentValue != null) {
                    return currentValue;
                }
            }

            // Keys mismatched or current value is null
            return write(key, index, currentValue, function);
        }

        protected T write(long key, int index, T currentValue, Long2ObjectFunction<T> function) {
            long write = lock.writeLock();
            try {
                T newValue = function.apply(key);
                keys[index] = key;
                values[index] = newValue;
                return newValue;
            } finally {
                lock.unlockWrite(write);
                onRemove(currentValue);
            }
        }
    }
}
