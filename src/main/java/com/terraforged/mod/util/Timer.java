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

import com.terraforged.mod.TerraForged;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class Timer {
    protected static final double NANO_TO_MS = 1.0 / TimeUnit.MILLISECONDS.toNanos(1);;

    protected final String name;
    protected final long interval;

    protected final AtomicLong timestamp = new AtomicLong();
    protected final AtomicLong sumDuration = new AtomicLong();
    protected final AtomicInteger count = new AtomicInteger();
    protected final ThreadLocal<Instance> localInstance;

    public Timer(String name, long interval, TimeUnit unit) {
        this.name = name;
        this.interval = unit.toNanos(interval);
        this.localInstance = ThreadLocal.withInitial(this::createInstance);
    }

    public Instance start() {
        return localInstance.get().punchIn();
    }

    public double getAverageNanos() {
        return sumDuration.get() / (double) count.get();
    }

    public double getAverageMS() {
        return getAverageNanos() * NANO_TO_MS;
    }

    public void submit(long duration) {
        sumDuration.addAndGet(duration);
        count.incrementAndGet();

        tick();
    }

    protected void tick() {
        long now = System.nanoTime();
        long time = timestamp.get();

        if (time == 0L) {
            timestamp.set(now);
            return;
        }

        if (now > time && timestamp.compareAndSet(time, now + interval)) {
            double average = (sumDuration.get() * NANO_TO_MS) / count.get();
            average = trim(average, 100);
            TerraForged.LOG.info("Average Time: {} = {}ms", name, average);
        }
    }

    protected Instance createInstance() {
        return new Instance(this);
    }

    protected static double trim(double d, int dp) {
        d = Math.round(d * dp);
        return d / dp;
    }

    public static class Instance implements AutoCloseable {
        protected final Timer timer;
        protected long sum;
        protected long time;

        public Instance(Timer timer) {
            this.timer = timer;
        }

        public Instance punchIn() {
            time = System.nanoTime();
            return this;
        }

        @Override
        public void close() {
            long now = System.nanoTime();

            if (time == -1) return;

            timer.submit(now - time);
            time = -1L;
            sum = 0L;
        }

        public long closeAndGet() {
            long now = System.nanoTime();
            if (time == -1) return sum;

            long duration = now - time;
            timer.submit(duration);
            time = -1L;
            sum = 0L;

            return duration;
        }

        public long closeAndAccumulate() {
            long now = System.nanoTime();
            if (time == -1) return sum;

            long duration = now - time;
            long accumulation = sum + duration;

            timer.submit(duration);
            time = -1L;
            sum = 0L;

            return accumulation;
        }

        public Instance thenStart(Timer timer) {
            long sum = closeAndAccumulate();

            var next = timer.start();
            next.sum = sum;

            return next;
        }
    }
}
