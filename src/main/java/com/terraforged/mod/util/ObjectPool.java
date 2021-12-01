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
