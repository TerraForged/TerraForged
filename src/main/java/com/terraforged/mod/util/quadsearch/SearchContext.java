/*
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

package com.terraforged.mod.util.quadsearch;

import com.terraforged.engine.concurrent.cache.SafeCloseable;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntBinaryOperator;

public class SearchContext implements SafeCloseable {

    IntBinaryOperator STATUS_OPERATOR = Math::min;

    private final long timeoutNS;
    private final AtomicInteger tasks = new AtomicInteger(0);
    private final AtomicInteger status = new AtomicInteger(Integer.MAX_VALUE);

    public SearchContext() {
        this(Long.MAX_VALUE, TimeUnit.DAYS);
    }

    public SearchContext(long timeout, TimeUnit unit) {
        long nanos = System.nanoTime() + unit.toNanos(timeout);
        timeoutNS = nanos < 0 ? Long.MAX_VALUE : nanos;
    }

    public boolean hasTimedOut() {
        return System.nanoTime() > timeoutNS;
    }

    public boolean hasTasks() {
        return tasks.get() > 0;
    }

    public boolean isComplete(int status) {
        return status > this.status.get() || hasTimedOut();
    }

    public void submit(int status) {
        this.status.accumulateAndGet(status, STATUS_OPERATOR);
    }

    public SearchContext startTask() {
        tasks.incrementAndGet();
        return this;
    }

    public void endTask() {
        tasks.decrementAndGet();
    }

    @Override
    public void close() {
        endTask();
    }
}
