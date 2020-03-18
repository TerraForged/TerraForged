package com.terraforged.core.util.concurrent.cache;

import com.terraforged.core.util.concurrent.ThreadPool;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;

public class Cache<K, V extends ExpiringEntry> implements Runnable {

    private final long expireMS;
    private final long intervalMS;
    private final Executor executor;
    private final Map<K, V> map = new ConcurrentHashMap<>();

    private volatile long timestamp = 0L;

    public Cache(long expireTime, long interval, TimeUnit unit) {
        this.expireMS = unit.toMillis(expireTime);
        this.intervalMS = unit.toMillis(interval);
        this.executor = ThreadPool.getCurrent();
    }

    public V computeIfAbsent(K k, Supplier<V> supplier) {
        return computeIfAbsent(k, o -> supplier.get());
    }

    public V computeIfAbsent(K k, Function<K, V> func) {
        V v = map.computeIfAbsent(k, func);
        queueUpdate();
        return v;
    }

    private void queueUpdate() {
        long now = System.currentTimeMillis();
        if (now - timestamp > intervalMS) {
            timestamp = now;
            executor.execute(this);
        }
    }

    @Override
    public void run() {
        final long now = timestamp;
        map.forEach((key, val) -> {
            if (now - val.getTimestamp() > expireMS) {
                map.remove(key);
            }
        });
    }
}
