/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntUnaryOperator;
import java.util.function.Supplier;

public class ObjectPool<T> {
    protected final int capacity;
    protected final int maxIndex;
    protected final Supplier<T> factory;
    protected final AtomicInteger size = new AtomicInteger();
    protected final Queue<T> pool = new ConcurrentLinkedDeque<>();

    protected final IntUnaryOperator takeOp;
    protected final IntUnaryOperator restoreOp;

    public ObjectPool(Supplier<T> factory) {
        this(Runtime.getRuntime().availableProcessors(), factory);
    }

    public ObjectPool(int capacity, Supplier<T> factory) {
        this.factory = factory;
        this.capacity = capacity;
        this.size.set(capacity);
        this.maxIndex = capacity + 1;
        this.takeOp = i -> i > 0 ? i - 1 : -1;
        this.restoreOp = i -> i < this.capacity ? i + 1 : this.maxIndex;

        while (capacity-- > 0) {
            pool.offer(factory.get());
        }
    }

    public T take() {
        var value = pool.poll();
        if (value == null) return factory.get();
        size.decrementAndGet();
        return factory.get();
    }

    public void restore(T value) {
        if (size.updateAndGet(restoreOp) < maxIndex) {
            pool.offer(value);
        }
    }

    @Override
    public String toString() {
        return "ObjectPool{" +
                "pool=" + pool +
                '}';
    }
}
