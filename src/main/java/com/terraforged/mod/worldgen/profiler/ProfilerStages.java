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

import com.terraforged.mod.TerraForged;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class ProfilerStages {
    private static final long INTERVAL_MS = TimeUnit.SECONDS.toMillis(10);

    public final GenStage starts = new GenStage("starts");
    public final GenStage refs = new GenStage("refs");
    public final GenStage biomes = new GenStage("biomes");
    public final GenStage noise = new GenStage("noise");
    public final GenStage carve = new GenStage("carve");
    public final GenStage surface = new GenStage("surface");
    public final GenStage decoration = new GenStage("decoration");

    private final long start = System.currentTimeMillis() + INTERVAL_MS * 2;
    private final AtomicLong timestamp = new AtomicLong(0);
    private final GenStage[] stages = {starts, refs, biomes, noise, carve, surface, decoration};

    public void reset() {
        for (var stage : stages) {
            stage.reset();
        }
    }

    public void tick() {
        long time = timestamp.get();
        long now = System.currentTimeMillis();

        if (time == 0L) {
            timestamp.set(now + INTERVAL_MS);
            if (now > start) reset();
            return;
        }

        if (now > time && timestamp.compareAndSet(time, now + INTERVAL_MS)) {
            TerraForged.LOG.info("Timings:");
            double sumAverage = 0;
            for (var stage : stages) {
                double average = stage.getAverageMS();
                sumAverage += average;
                average = trim(average, 100);
                TerraForged.LOG.info(" - {} = {}ms", stage.name(), average);
            }

            sumAverage = trim(sumAverage, 100);
            TerraForged.LOG.info(" Chunk Average = {}ms", sumAverage);
        }
    }

    private static double trim(double d, int dp) {
        d = Math.round(d * dp);
        return d / dp;
    }
}
