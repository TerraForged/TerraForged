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

import com.terraforged.mod.Environment;
import com.terraforged.noise.util.NoiseUtil;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2ObjectFunction;
import net.minecraft.util.Mth;

import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;
import java.util.function.Consumer;
import java.util.function.IntFunction;

public class LossyCache<T> implements LongCache<T> {
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

    @Override
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

    protected static int hash(long l) {
        return (int) HashCommon.mix(l);
    }

    public static <T> LongCache<T> of(int capacity, IntFunction<T[]> constructor) {
        return of(capacity, constructor, t -> {});
    }

    public static <T> LongCache<T> of(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        return new LossyCache<>(capacity, constructor, removalListener);
    }

    public static <T> LongCache<T> concurrent(int capacity, IntFunction<T[]> constructor) {
        return concurrent(capacity, constructor, t -> {});
    }

    public static <T> LongCache<T> concurrent(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        return concurrent(capacity, Environment.CORES, constructor, removalListener);
    }

    public static <T> LongCache<T> concurrent(int capacity, int concurrency, IntFunction<T[]> constructor, Consumer<T> removalListener) {
        return new Concurrent<>(capacity, concurrency, constructor, removalListener);
    }

    public static class Stamped<T> extends LossyCache<T> {
        protected final StampedLock lock = new StampedLock();

        public Stamped(int capacity, IntFunction<T[]> constructor, Consumer<T> removalListener) {
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

            if (!lock.validate(optStamp)) {
                // Write occurred during the optimistic read so obtain a full read lock
                long read = lock.readLock();

                try {
                    currentKey = keys[index];
                    currentValue = values[index];
                } finally {
                    lock.unlockRead(read);
                }
            }

            if (currentKey == key && currentValue != null) {
                return currentValue;
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

    public static class Concurrent<T> implements LongCache<T> {
        protected static final int HASH_BITS = 0x7fffffff;

        protected final int mask;
        protected final Stamped<T>[] buckets;

        public Concurrent(int capacity, int concurrency, IntFunction<T[]> constructor, Consumer<T> removalListener) {
            concurrency = Mth.smallestEncompassingPowerOfTwo(concurrency);
            capacity = NoiseUtil.floor(((float) capacity / concurrency));

            this.mask = concurrency - 1;
            //noinspection unchecked
            this.buckets = new Stamped[concurrency];

            for (int i = 0; i < concurrency; i++) {
                this.buckets[i] = new Stamped<>(capacity, constructor, removalListener);
            }
        }

        @Override
        public T computeIfAbsent(long key, Long2ObjectFunction<T> function) {
            return buckets[index(key)].computeIfAbsent(key, function);
        }

        protected int index(long key) {
            return spread(key) & mask;
        }

        protected static int spread(long h) {
            return (int) (h ^ (h >>> 16)) & HASH_BITS;
        }
    }
}
