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

import com.terraforged.core.concurrent.Resource;
import com.terraforged.mod.util.StringUtils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.function.LongBinaryOperator;
import java.util.stream.Stream;

public class Timing {

    private static final long ROLLOVER = 10_000;
    private static final LongBinaryOperator SHORTEST = Math::min;
    private static final LongBinaryOperator LONGEST = Math::max;

    private static final int PREFIX_WIDTH;
    private static final int MIN_TIME_WIDTH = 2 + 1 + 3;
    private static final int MAX_TIME_WIDTH = 4 + 1 + 3;
    private static final int COUNT_WIDTH = 5;

    static {
        PREFIX_WIDTH = Stream.of(Section.values()).mapToInt(s -> s.name().length()).max().orElse(0) + 1;
    }

    private final String prefix;
    private final AtomicLong time = new AtomicLong();
    private final AtomicLong samples = new AtomicLong();
    private final AtomicLong longest = new AtomicLong(Long.MIN_VALUE);
    private final AtomicLong shortest = new AtomicLong(Long.MAX_VALUE);

    public Timing(Section section) {
        StringBuilder sb = new StringBuilder(section.name()).append(':');
        while (sb.length() < PREFIX_WIDTH) {
            sb.append(' ');
        }
        prefix = sb.append("  ").toString();
    }

    public String toText() {
        long count = samples.get();
        String min = String.format("%.3f", getFastestMS());
        String max = String.format("%.3f", getSlowestMS());
        String average = String.format("%.3f", getAverageMS(count));

        try (Resource<StringBuilder> resource = StringUtils.retain()) {
            StringBuilder sb = resource.get().append(prefix);
            StringUtils.padAppend(sb, "Min: ", min, MIN_TIME_WIDTH).append("ms, ");
            StringUtils.padAppend(sb, "Max: ", max, MAX_TIME_WIDTH).append("ms, ");
            StringUtils.padAppend(sb, "Average: ", average, MIN_TIME_WIDTH).append("ms, ");
            StringUtils.padAppend(sb, "Samples: ", String.valueOf(count), COUNT_WIDTH);
            return sb.toString();
        }
    }

    public void reset() {
        time.set(0);
        samples.set(0);
    }

    public void inc(long durationNanos) {
        if (durationNanos > 0) {
            long count = samples.incrementAndGet();
            long total = time.addAndGet(durationNanos);

            longest.accumulateAndGet(durationNanos, LONGEST);
            shortest.accumulateAndGet(durationNanos, SHORTEST);

            if (count >= ROLLOVER) {
                long avg = total / count;
                if (samples.compareAndSet(count, 1)) {
                    time.set(avg);
                }
            }
        }
    }

    public double getAverageMS() {
        return getAverageMS(samples.get());
    }

    public double getFastestMS() {
        long time = shortest.get();
        if (time == Long.MAX_VALUE) {
            return 0.0;
        }
        return toMillis(time);
    }

    public double getSlowestMS() {
        long time = longest.get();
        if (time == Long.MIN_VALUE) {
            return 0.0;
        }
        return toMillis(time);
    }

    private double getAverageMS(long count) {
        if (count <= 0) {
            return 0.0;
        }
        long total = time.get();
        if (total == 0) {
            return 0.0;
        }
        return toMillis(total / count);
    }

    private double toMillis(long nanos) {
        return nanos / 1_000_000D;
    }

    private static void padAppend(StringBuilder sb, String prefix, String text, int width) {
        sb.append(prefix);
        for (int pad = width - text.length(); pad > 0; pad--) {
            sb.append(' ');
        }
        sb.append(text);
    }
}
