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

package com.terraforged.mod.chunk.profiler;

import com.terraforged.core.concurrent.cache.map.LongMap;
import com.terraforged.core.concurrent.cache.map.StampedLockLongMap;
import com.terraforged.mod.Log;
import com.terraforged.mod.util.Environment;
import com.terraforged.mod.util.StringUtils;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

public class GenProfiler {

    private final boolean enabled;
    private final long intervalNanos;
    private final AtomicLong lastUpdate;
    private final LongMap<ThreadContext> contexts = new StampedLockLongMap<>(16);
    private final Timing[] timings = Stream.of(Section.values()).map(Timing::new).toArray(Timing[]::new);

    public GenProfiler(long interval, TimeUnit unit) {
        this.enabled = Environment.profile();
        this.intervalNanos = unit.toNanos(interval);
        this.lastUpdate = new AtomicLong(System.currentTimeMillis());
    }

    public Ticket start(Section section) {
        if (enabled) {
            long now = System.nanoTime();
            checkUpdate(now);
            return contexts.computeIfAbsent(Thread.currentThread().getId(), ThreadContext::new).retain(section, now);
        }
        return Ticket.NONE;
    }

    private Timing getTiming(Section section) {
        return timings[section.ordinal()];
    }

    private void checkUpdate(long now) {
        long last = lastUpdate.get();
        long time = now - last;
        if (time > intervalNanos && lastUpdate.compareAndSet(last, now)) {
            Log.debug("TIMINGS REPORT:");

            int width = 0;
            double total = 0L;
            for (int i = 0; i < timings.length; i++) {
                Timing timing = timings[i];
                String line = timing.toText();
                Log.debug("[{}] {}", i, line);

                total += timing.getAverageMS();
                width = Math.max(width, line.length() + 4);
            }

            Log.debug("Average chunk generation time: {}ms", (long) total);
            Log.debug(StringUtils.repeat('-', width));
        }
    }

    private class ThreadContext {

        private final Ticket[] tickets = Stream.of(Section.values())
                .map(GenProfiler.this::getTiming)
                .map(Ticket::new)
                .toArray(Ticket[]::new);

        private ThreadContext(long ignored) {

        }

        private Ticket retain(Section section, long now) {
            return tickets[section.ordinal()].retain(now);
        }
    }
}
