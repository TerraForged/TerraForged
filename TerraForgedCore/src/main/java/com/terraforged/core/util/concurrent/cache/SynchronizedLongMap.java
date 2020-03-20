package com.terraforged.core.util.concurrent.cache;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

import java.util.function.LongFunction;
import java.util.function.Predicate;

public class SynchronizedLongMap<V> {

    private final Object lock;
    private final Long2ObjectOpenHashMap<V> map;

    public SynchronizedLongMap(int size) {
        this.map = new Long2ObjectOpenHashMap<>(size);
        this.lock = this;
    }

    public void remove(long key) {
        synchronized (lock) {
            map.remove(key);
        }
    }

    public void put(long key, V v) {
        synchronized (lock) {
            map.put(key, v);
        }
    }

    public V get(long key) {
        synchronized (lock) {
            return map.get(key);
        }
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        synchronized (lock) {
            return map.computeIfAbsent(key, func);
        }
    }

    public void removeIf(Predicate<V> predicate) {
        synchronized (lock) {
            ObjectIterator<Long2ObjectMap.Entry<V>> iterator = map.long2ObjectEntrySet().fastIterator();
            while (iterator.hasNext()) {
                if (predicate.test(iterator.next().getValue())) {
                    iterator.remove();
                }
            }
        }
    }
}
