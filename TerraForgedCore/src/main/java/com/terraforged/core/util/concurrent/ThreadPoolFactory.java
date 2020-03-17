package com.terraforged.core.util.concurrent;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

// As DefaultThreadPool but with custom thread names
public class ThreadPoolFactory implements ThreadFactory {

    private final ThreadGroup group;
    private final AtomicInteger threadNumber = new AtomicInteger(1);

    public ThreadPoolFactory() {
        group = Thread.currentThread().getThreadGroup();
    }

    @Override
    public Thread newThread(Runnable task) {
        Thread thread = new Thread(group, task);
        thread.setDaemon(true);
        thread.setName("TerraForged-Worker-" + threadNumber.getAndIncrement());
        return thread;
    }
}
