package com.terraforged.core.util.concurrent.cache;

import com.terraforged.core.util.concurrent.ThreadPool;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinTask;

public class CacheEntry<T> implements ExpiringEntry {

    private volatile long timestamp;
    private final ForkJoinTask<T> task;

    public CacheEntry(ForkJoinTask<T> task) {
        this.task = task;
        this.timestamp = System.currentTimeMillis();
    }

    @Override
    public long getTimestamp() {
        return timestamp;
    }

    public boolean isDone() {
        return task.isDone();
    }

    public T get() {
        return task.join();
    }

    public static <T> CacheEntry<T> supplyAsync(Callable<T> callable, ThreadPool executor) {
        return new CacheEntry<>(executor.submit(callable));
    }
}
