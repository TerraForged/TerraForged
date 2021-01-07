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

    private static final String MESSAGE = "Server deadlock detected! A single chunk has taken over %s to generate. Crashed whilst generating: %s %s";

    private final IChunk chunk;
    private final TFChunkGenerator generator;

    protected DeadlockException(String phase, Object identity, long timeMS, TimingStack stack, IChunk chunk, TFChunkGenerator generator, Thread thread) {
        super(createHeader(phase, identity, timeMS, stack), thread.getStackTrace());
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

    private static String createHeader(String phase, Object identity, long timeMS, TimingStack stack) {
        return createMessage(phase, identity, timeMS) + createTimingsMessage(stack);
    }

    private static String createMessage(String phase, Object identity, long timeMS) {
        String phaseName = nonNullStringValue(phase, "unknown");
        String identName = nonNullStringValue(identity, "unknown");
        String time = timeMS > 1000L ? (timeMS / 1000L) + "s" : timeMS + "ms";
        return String.format(MESSAGE, time, phaseName, identName);
    }

    private static String createTimingsMessage(TimingStack stack) {
        if (stack.getSize() > 0) {
            StringBuilder sb = new StringBuilder(2048);
            appendStackTitle(sb, stack);

            AtomicInteger counter = new AtomicInteger(1);
            stack.iterate(sb, (type, identity, time, ctx) -> {
                ctx.append("\n\t\t\t\t");
                appendIndex(ctx, counter.getAndIncrement());
                appendType(ctx, type);
                appendTime(ctx, time);
                appendIdentity(ctx, identity);
            });

            return sb.toString();
        }
        return "";
    }

    private static void appendStackTitle(StringBuilder sb, TimingStack stack) {
        if (stack.getSize() == 0) {
            sb.append("\n\t\t\tThe slowest performing feature/structure before that was:");
        } else {
            sb.append("\n\t\t\tThe ").append(stack.getSize()).append(" slowest performing features/structures before that were:");
        }
    }

    private static void appendIndex(StringBuilder sb, int index) {
        String text = index < 10 ? "0" + index : String.valueOf(index);
        sb.append("[").append(text).append("]\t");
    }

    private static void appendType(StringBuilder sb, String type) {
        sb.append("Type: ").append(type);
        pad(sb, type.length(), 16);
    }

    private static void appendTime(StringBuilder sb, long ms) {
        String text = ms > 1000L ? String.format("%.3fs", ms / 1000D) : ms + "ms";
        sb.append("\tTime: ").append(text);
        pad(sb, text.length(), 16);
    }

    private static void appendIdentity(StringBuilder sb, Object identity) {
        sb.append("Identity: ").append(identity);
    }

    private static void pad(StringBuilder sb, int len, int width) {
        int space = width - len;
        int tabs = space / 4;
        if (tabs * 4 <= space) {
            tabs = Math.max(1, tabs - 1);
        }
        for (int i = 0; i < tabs; i++) {
            sb.append("\t");
        }
    }
}
