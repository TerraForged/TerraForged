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
