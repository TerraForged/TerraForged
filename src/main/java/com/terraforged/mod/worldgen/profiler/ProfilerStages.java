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

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class ProfilerStages {
    // @formatter:off
    public final GenStage starts     = new GenStage("Starts:    ");
    public final GenStage refs       = new GenStage("Refs:      ");
    public final GenStage biomes     = new GenStage("Biomes:    ");
    public final GenStage noise      = new GenStage("Noise:     ");
    public final GenStage carve      = new GenStage("Carvers: ");
    public final GenStage surface    = new GenStage("Surface: ");
    public final GenStage decoration = new GenStage("Features:");
    // @formatter:on

    private final long start = System.currentTimeMillis() + 10_000;
    private final AtomicInteger chunkCount = new AtomicInteger();
    private final AtomicLong timestamp = new AtomicLong(0);
    private final GenStage[] stages = {starts, refs, biomes, noise, carve, surface, decoration};

    private final List<String> debugInfoCache = new ArrayList<>();

    public void incrementChunks() {
        chunkCount.incrementAndGet();
    }

    public void reset() {
        chunkCount.set(0);

        for (var stage : stages) {
            stage.reset();
        }
    }

    public void tick(long interval) {
        tick(interval, debugInfoCache);

        for (var line : debugInfoCache) {
            TerraForged.LOG.info(line);
        }
    }

    public void addDebugInfo(long interval, List<String> lines) {
        tick(interval, debugInfoCache);

        lines.addAll(debugInfoCache);
    }

    private void tick(long interval, List<String> lines) {
        long time = timestamp.get();
        long now = System.currentTimeMillis();

        if (time == 0L) {
            timestamp.set(now + interval * 2);
            if (now > start) reset();
            return;
        }

        if (now > time && timestamp.compareAndSet(time, now + interval)) {
            lines.clear();
            lines.add("");
            lines.add("[World-Gen Performance]");

            double sumAverage = 0;
            for (var stage : stages) {
                double average = stage.getAverageMS();
                sumAverage += average;
                lines.add(String.format("%s %.2fms", stage.name(), average));
            }

            lines.add(String.format("Chunk Average: %.2fms", sumAverage));
            lines.add(String.format("Chunk Count:   %s", chunkCount.get()));
            lines.add("");
        }
    }
}
