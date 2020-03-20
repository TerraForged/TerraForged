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

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;

public class ThreadPool implements Executor {

    private static final ThreadPool instance = new ThreadPool("TF", defaultPoolSize());
    private static final Object lock = new Object();

    private final ForkJoinPool service;

    private ThreadPool(String name, int size) {
        this.service = ThreadPool.createPool(size, name);
    }

    @Override
    public void execute(Runnable command) {
        service.submit(command);
    }

    public <T> ForkJoinTask<?> submit(Runnable runnable) {
        return service.submit(runnable);
    }

    public <T> ForkJoinTask<T> submit(Callable<T> callable) {
        return service.submit(callable);
    }

    public Batcher batcher(int size) {
        return new AsyncBatcher(service, size);
    }

    public static ThreadPool getPool() {
        return instance;
    }

    public static ThreadPool create(int size) {
        return new ThreadPool("Pool", Math.max(1, size));
    }

    private static int defaultPoolSize() {
        int threads = Runtime.getRuntime().availableProcessors();
        return Math.max(1, (int) ((threads / 3F) * 2F));
    }

    public static ForkJoinPool createPool(int size, String name) {

        return new ForkJoinPool(size, new WorkerFactory.ForkJoin(name), null, true);
    }
}
