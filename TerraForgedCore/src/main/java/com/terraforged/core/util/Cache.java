/*
 *
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

package com.terraforged.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Cache<K, V> {

    private final long lifespan;
    private final long interval;
    private final Map<K, CachedValue<V>> cache;

    private long lastUpdate = 0L;

    public Cache(long lifespan, long interval, TimeUnit unit) {
        this(lifespan, interval, unit, () -> new HashMap<>(100));
    }

    public Cache(long lifespan, long interval, TimeUnit unit, Supplier<Map<K, CachedValue<V>>> backing) {
        this.lifespan = unit.toMillis(lifespan);
        this.interval = unit.toMillis(interval);
        this.cache = backing.get();
    }

    public V get(K id) {
        update();
        CachedValue<V> value = cache.get(id);
        if (value != null) {
            return value.getValue();
        }
        return null;
    }

    public void put(K id, V region) {
        update();
        cache.put(id, new CachedValue<>(region));
    }

    public void drop(K id) {
        update();
    }

    public boolean contains(K id) {
        return cache.containsKey(id);
    }

    private void update() {
        long time = System.currentTimeMillis();
        if (time - lastUpdate < interval) {
            return;
        }
        cache.values().removeIf(value -> time - value.time >= lifespan);
        lastUpdate = time;
    }

    public static class CachedValue<T> {

        private final T value;
        private long time;

        private CachedValue(T value) {
            this.value = value;
            this.time = System.currentTimeMillis();
        }

        private T getValue() {
            time = System.currentTimeMillis();
            return value;
        }
    }

    public static <K, V> Cache<K, V> concurrent(long lifespan, long interval, TimeUnit unit) {
        return new Cache<>(lifespan, interval, unit, () -> new ConcurrentHashMap<>(100));
    }
}
