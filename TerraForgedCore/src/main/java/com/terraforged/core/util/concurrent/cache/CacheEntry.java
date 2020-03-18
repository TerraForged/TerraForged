package com.terraforged.core.util.concurrent.cache;

import java.util.concurrent.Callable;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;
import java.util.concurrent.FutureTask;

public class CacheEntry<T> extends FutureTask<T> implements ExpiringEntry {

    private volatile long timestamp;

    private CacheEntry(Callable<T> supplier) {
        super(supplier);
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public T get() {
        try {
            this.timestamp = System.currentTimeMillis();
            return super.get();
        } catch (Throwable t) {
            throw new CompletionException(t);
        }
    }

    public static <T> CacheEntry<T> supplyAsync(Callable<T> supplier, Executor executor) {
        CacheEntry<T> entry = new CacheEntry<>(supplier);
        executor.execute(entry);
        return entry;
    }
}
