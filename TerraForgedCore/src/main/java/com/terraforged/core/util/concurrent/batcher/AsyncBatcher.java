package com.terraforged.core.util.concurrent.batcher;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public class AsyncBatcher implements Batcher {

    private final List<Future<?>> tasks;
    private final ExecutorService executor;

    public AsyncBatcher(ExecutorService executor, int size) {
        this.executor = executor;
        this.tasks = new ArrayList<>(size);
    }

    @Override
    public void submit(Runnable task) {
        tasks.add(executor.submit(task));
    }

    @Override
    public void submit(Callable<?> task) {
        tasks.add(executor.submit(task));
    }

    @Override
    public void close() {
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
}
