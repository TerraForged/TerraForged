package com.terraforged.core.util;

import java.util.HashMap;
import java.util.Map;
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
}
