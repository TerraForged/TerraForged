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

package com.terraforged.mod.util.crash.watchdog;

import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.util.crash.watchdog.timings.TimingStack;
import net.minecraft.world.chunk.IChunk;

import java.util.concurrent.atomic.AtomicInteger;

public class DeadlockException extends UncheckedException {

    private static final String MESSAGE = "World-gen deadlock detected!"
            + "\n"
            + "\n\t\t\tA single chunk has taken over %s to generate!"
            + "\n\t\t\tWe crashed whilst generating: %s %s"
            + "\n\t\t\tIt had been generating for: %s (%s)";

    private static final String TABLE_HEADER_ONE = "\n\t\t\tThe slowest performing feature/structure before that was:";
    private static final String TABLE_HEADER_MULT = "\n\t\t\tThe %s slowest performing features/structures before that were:";
    private static final String TABLE_FORMAT = "\n\t\t\t\t%1$-8s%2$-12s%3$-10s%4$-10s%5$-1s";
    private static final String TABLE_FOOTER = "\n";

    private final IChunk chunk;
    private final TFChunkGenerator generator;

    protected DeadlockException(String phase, Object identity, long totalTime, long itemTime, TimingStack stack, IChunk chunk, TFChunkGenerator generator, Thread thread) {
        super(createMessage(phase, identity, totalTime, itemTime, stack), thread.getStackTrace());
        this.chunk = chunk;
        this.generator = generator;
        setStackTrace(thread.getStackTrace());
    }

    public IChunk getChunk() {
        return chunk;
    }

    public TFChunkGenerator getGenerator() {
        return generator;
    }

    private static String createMessage(String phase, Object identity, long totalTime, long itemTime, TimingStack stack) {
        return createSummary(phase, identity, totalTime, itemTime) + createTable(stack, totalTime);
    }

    private static String createSummary(String phase, Object identity, long totalTime, long itemTime) {
        String phaseName = nonNullStringValue(phase, "unknown");
        String identName = nonNullStringValue(identity, "unknown");
        String total = getTime(totalTime);
        String item = getTime(itemTime);
        String rating = getImpact(itemTime, totalTime);
        return String.format(MESSAGE, total, phaseName, identName, item, rating);
    }

    private static String createTable(TimingStack stack, long totalTime) {
        if (stack.getSize() > 0) {
            StringBuilder sb = new StringBuilder(2048);
            sb.append(getTableTitle(stack));
            sb.append(String.format(TABLE_FORMAT, "Index", "Type", "Time", "Impact", "Identity"));

            AtomicInteger counter = new AtomicInteger(1);
            stack.iterate(sb, (type, identity, time, ctx) -> {
                String index = getIndex(counter.getAndIncrement());
                String duration = getTime(time);
                String percentage = getImpact(time, totalTime);
                ctx.append(String.format(TABLE_FORMAT, index, type, duration, percentage, identity));
            });

            return sb.append(TABLE_FOOTER).toString();
        }
        return "";
    }

    private static String getTableTitle(TimingStack stack) {
        int size = stack.getSize();
        if (size == 0) {
            return TABLE_HEADER_ONE;
        }
        return String.format(TABLE_HEADER_MULT, size);
    }

    private static String getIndex(int index) {
        return index < 10 ? "[0" + index + "]" : "[" + index + "]";
    }

    private static String getTime(long timeMS) {
        return timeMS > 1000L ? String.format("%.3fs", timeMS / 1000D) : timeMS + "ms";
    }

    private static String getImpact(long timeMS, long totalMS) {
        return String.format("%.2f%%", (100D * timeMS) / totalMS);
    }
}
