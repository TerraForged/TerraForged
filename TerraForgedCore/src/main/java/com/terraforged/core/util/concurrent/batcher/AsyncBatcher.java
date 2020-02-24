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
