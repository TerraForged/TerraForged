package com.terraforged.core.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// As DefaultThreadPool but with custom thread names
public class WorkerFactory implements ThreadFactory {

    private final String prefix;
    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public WorkerFactory(String name) {
        group = Thread.currentThread().getThreadGroup();
        prefix = name + "-Worker-";
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new Thread(group, task);
        thread.setDaemon(true);
        thread.setName(prefix + threadNumber.getAndIncrement());
        return thread;
    }
}
