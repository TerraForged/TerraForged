package com.terraforged.core.util.concurrent;

import com.terraforged.core.util.concurrent.batcher.AsyncBatcher;
import com.terraforged.core.util.concurrent.batcher.Batcher;
import com.terraforged.core.util.concurrent.batcher.SyncBatcher;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

public class ThreadPool {

    public static final int DEFAULT_POOL_SIZE = defaultPoolSize();

    private static final Object lock = new Object();

    private static ThreadPool instance = new ThreadPool();

    private final int poolSize;
    private final ExecutorService service;

    private ThreadPool() {
        this.service = ForkJoinPool.commonPool();
        this.poolSize = -1;
    }

    public ThreadPool(int size) {
        this.service = Executors.newFixedThreadPool(size);
        this.poolSize = size;
    }

    public void shutdown() {
        if (poolSize > 0) {
            service.shutdown();
        }
    }

    public <T> Future<?> submit(Runnable runnable) {
        return service.submit(runnable);
    }

    public <T> Future<T> submit(Callable<T> callable) {
        return service.submit(callable);
    }

    public Batcher batcher(int size) {
        if (this.poolSize != -1 && this.poolSize < 3) {
            return new SyncBatcher();
        }
        return new AsyncBatcher(service, size);
    }

    public static ThreadPool getFixed(int size) {
        synchronized (lock) {
            if (instance.poolSize != size) {
                instance.shutdown();
                instance = new ThreadPool(size);
            }
            return instance;
        }
    }

    public static ThreadPool getFixed() {
        synchronized (lock) {
            if (instance.poolSize == -1) {
                instance = new ThreadPool(ThreadPool.DEFAULT_POOL_SIZE);
            }
            return instance;
        }
    }

    public static ThreadPool getCommon() {
        synchronized (lock) {
            if (instance.poolSize != -1) {
                instance.shutdown();
                instance = new ThreadPool();
            }
            return instance;
        }
    }

    public static void shutdownCurrent() {
        synchronized (lock) {
            instance.shutdown();
            // replace with the common pool
            instance = new ThreadPool();
        }
    }

    private static int defaultPoolSize() {
        int threads = Runtime.getRuntime().availableProcessors();
        return Math.max(1, (threads / 3) * 2);
    }
}
