package com.terraforged.core.util.concurrent.cache;

import com.terraforged.core.util.concurrent.ThreadPool;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class Cache<V extends ExpiringEntry> implements Runnable {

    private final long expireMS;
    private final long intervalMS;
    private final SynchronizedLongMap<V> map;
    private final ThreadPool threadPool = ThreadPool.getPool();

    private volatile long timestamp = 0L;

    public Cache(long expireTime, long interval, TimeUnit unit) {
        this.expireMS = unit.toMillis(expireTime);
        this.intervalMS = unit.toMillis(interval);
        this.map = new SynchronizedLongMap<>(100);
    }

    public void remove(long key) {
        map.remove(key);
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
            threadPool.submit(this);
        }
    }

    @Override
    public void run() {
        final long now = timestamp;
        map.removeIf(val -> now - val.getTimestamp() > expireMS);
    }
}
