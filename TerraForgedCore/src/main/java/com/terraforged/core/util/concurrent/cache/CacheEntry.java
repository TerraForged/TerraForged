package com.terraforged.core.util.concurrent.cache;

import java.util.concurrent.Executor;
import java.util.function.Supplier;

public class CacheEntry<T> implements Runnable, ExpiringEntry {

    private volatile T result;
    private volatile long timestamp;

    private final Supplier<T> supplier;

    private CacheEntry(Supplier<T> supplier) {
        this.supplier = supplier;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public void run() {
        this.result = supplier.get();
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isDone() {
        return result != null;
    }

    public T get() {
        T value = result;
        if (value == null) {
            value = getNow();
        }
        return value;
    }

    private T getNow() {
        T result = supplier.get();
        this.result = result;
        return result;
    }

    public static <T> CacheEntry<T> supplyAsync(Supplier<T> supplier, Executor executor) {
        CacheEntry<T> entry = new CacheEntry<>(supplier);
        executor.execute(entry);
        return entry;
    }
}
