package com.terraforged.core.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class ThreadPool {

    public static final int DEFAULT_POOL_SIZE = Math.max(2, Runtime.getRuntime().availableProcessors() / 2);

    private static final Object lock = new Object();

    private static ThreadPool instance = new ThreadPool();

    private final int size;
    private final ExecutorService service;

    private ThreadPool() {
        this.service = ForkJoinPool.commonPool();
        this.size = -1;
    }

    public ThreadPool(int size) {
        this.service = Executors.newFixedThreadPool(size);
        this.size = size;
    }

    public void shutdown() {
        if (size > 0) {
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
        return new Batcher(size);
    }

    public class Batcher implements AutoCloseable {

        private final List<Future<?>> tasks;

        public Batcher(int size) {
            tasks = new ArrayList<>(size);
        }

        public void submit(Runnable task) {
            tasks.add(ThreadPool.this.submit(task));
        }

        public void submit(Callable<?> task) {
            tasks.add(ThreadPool.this.submit(task));
        }

        public void await() {
            boolean hasMore = true;
            while (hasMore) {
                hasMore = false;
                for (Future<?> future : tasks) {
                    if (!future.isDone()) {
                        hasMore = true;
                    }
                }
            }
            tasks.clear();
        }

        @Override
        public void close() {
            await();
        }
    }

    public static ThreadPool getFixed(int size) {
        synchronized (lock) {
            if (instance.size != size) {
                instance.shutdown();
                instance = new ThreadPool(size);
            }
            return instance;
        }
    }

    public static ThreadPool getFixed() {
        synchronized (lock) {
            if (instance.size == -1) {
                instance = new ThreadPool(ThreadPool.DEFAULT_POOL_SIZE);
            }
            return instance;
        }
    }

    public static ThreadPool getCommon() {
        synchronized (lock) {
            if (instance.size != -1) {
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
}
