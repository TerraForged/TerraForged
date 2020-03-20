package com.terraforged.core.util.concurrent.cache;

import com.terraforged.core.util.concurrent.ThreadPool;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;
import java.util.function.Supplier;

public class Cache<V extends ExpiringEntry> implements Runnable {

    private final long expireMS;
    private final long intervalMS;
    private final SynchronizedLongMap<V> map;

    private volatile long timestamp = 0L;

    public Cache(long expireTime, long interval, TimeUnit unit) {
        this.expireMS = unit.toMillis(expireTime);
        this.intervalMS = unit.toMillis(interval);
        this.map = new SynchronizedLongMap<>(100);
    }

    public V computeIfAbsent(long key, Supplier<V> supplier) {
        return computeIfAbsent(key, o -> supplier.get());
    }

    public V computeIfAbsent(long key, LongFunction<V> func) {
        V v = map.computeIfAbsent(key, func);
        queueUpdate();
        return v;
    }

    private void queueUpdate() {
        long now = System.currentTimeMillis();
        if (now - timestamp > intervalMS) {
            timestamp = now;
            ThreadPool.getDefaultPool().execute(this);
        }
    }

    @Override
    public void run() {
        final long now = timestamp;
        map.removeIf(val -> now - val.getTimestamp() > expireMS);
    }
}
