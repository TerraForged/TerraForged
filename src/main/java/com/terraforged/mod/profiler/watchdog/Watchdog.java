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

package com.terraforged.mod.profiler.watchdog;

import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.profiler.crash.CrashHandler;
import com.terraforged.mod.profiler.crash.WorldGenException;
import net.minecraft.world.chunk.IChunk;

import java.util.Iterator;
import java.util.ListIterator;

public class Watchdog implements Runnable {

    private static final long WARN_TIME = 50;
    private static final long CRASH_TIME = 30_000L;
    private static final long MIN_CRASH_TIME = 5_000L;
    private static final long CHECK_INTERVAL = 5_250L;
    private static final ThreadLocal<WatchdogCtx> TASKS = ThreadLocal.withInitial(WatchdogCtx::new);

    static {
        Thread thread = new Thread(new Watchdog());
        thread.setName("TF-Watchdog");
        thread.start();
    }

    private int lastSize = 0;
    private ListIterator<WatchdogCtx> lastIterator;

    @Override
    public void run() {
        try {
            while (running()) {
                Iterator<WatchdogCtx> iterator = nextIterator();

                if (iterator.hasNext()) {
                    long now = System.currentTimeMillis();
                    while (iterator.hasNext()) {
                        WatchdogCtx next = iterator.next();
                        next.check(now);
                    }
                }
            }
        } catch (DeadlockException e) {
            e.printStackTrace();
            CrashHandler.handle(e.getChunk(), e.getGenerator(), new WorldGenException(Profiler.DECORATION, e));
        }
    }

    private Iterator<WatchdogCtx> nextIterator() {
        int size = WatchdogCtx.CONTEXTS.size();
        if (lastSize != size || lastIterator == null) {
            lastSize = size;
            lastIterator = WatchdogCtx.CONTEXTS.listIterator();
        } else {
            while (lastIterator.hasPrevious()) {
                lastIterator.previous();
            }
        }
        return lastIterator;
    }

    private boolean running() {
        try {
            Thread.sleep(CHECK_INTERVAL);
        } catch (Throwable t) {
            t.printStackTrace();
            return true;
        }
        return true;
    }

    public static WatchdogContext punchIn(IChunk chunk, TFChunkGenerator generator, long duration) {
        if (duration > 0) {
            WatchdogCtx context = TASKS.get();
            if (context.set(chunk, generator, duration)) {
                return context;
            }
        }
        return WatchdogContext.NONE;
    }

    public static WarnTimer getWarnTimer() {
        return new WarnTimer(ConfigManager.GENERAL.load().getLong("feature_warn_time", WARN_TIME));
    }

    public static long getWatchdogHangTime() {
        long time = ConfigManager.GENERAL.load().getLong("server_deadlock_timeout", CRASH_TIME);
        if (time <= 0) {
            return -1L;
        }
        return Math.max(time, MIN_CRASH_TIME);
    }
}
