/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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
