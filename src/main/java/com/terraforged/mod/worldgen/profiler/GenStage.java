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

package com.terraforged.mod.worldgen.profiler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class GenStage {
    private static final double NANO_TO_MS = 1.0 / TimeUnit.MILLISECONDS.toNanos(1);

    private final AtomicLong time = new AtomicLong();
    private final AtomicInteger count = new AtomicInteger();

    private final String name;

    public GenStage(String name) {
        this.name = name;
    }

    public String name() {
        return name;
    }

    public void reset() {
        time.set(0);
        count.set(0);
    }

    public GenTimer start() {
        return new GenTimer(this, System.nanoTime());
    }

    public void push(long duration) {
        time.addAndGet(duration);
        count.incrementAndGet();
    }

    public double getAverageNanos() {
        return time.get() / (double) count.get();
    }

    public double getAverageMS() {
        return getAverageNanos() * NANO_TO_MS;
    }
}
