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

import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.profiler.crash.CrashHandler;
import com.terraforged.mod.profiler.crash.WorldGenException;
import net.minecraft.world.chunk.IChunk;

public class Watchdog implements Runnable {

    public static final String FEATURE_WARN_KEY = "feature_warn_time";
    public static final String CHUNK_TIMEOUT_KEY = "chunkgen_timeout";

    private static final long WARN_TIME = 50;
    private static final long CRASH_TIME = 30_000L;
    private static final long MIN_CRASH_TIME = 5_000L;
    private static final long CHECK_INTERVAL = 5_251L;

    private static final Watchdog INSTANCE = new Watchdog();

    private final ContextQueue contextQueue = new ContextQueue();
    private final ThreadLocal<WarnTimer> timerPool = ThreadLocal.withInitial(Watchdog::getWarnTimer);
    private final ThreadLocal<WatchdogContext> contextPool = ThreadLocal.withInitial(WatchdogCtx::new);

    static {
        ThreadPools.scheduleFixed(INSTANCE, CHECK_INTERVAL);
    }

    @Override
    public void run() {
        try {
            WatchdogContext[] queue = contextQueue.get();
            if (queue.length > 0) {
                long now = System.currentTimeMillis();
                for (WatchdogContext context : queue) {
                    context.check(now);
                }
            }
        } catch (ChunkTimeoutException e) {
            e.printStackTrace();
            CrashHandler.handle(e.getChunk(), e.getGenerator(), new WorldGenException(Profiler.DECORATION, e));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    public static WatchdogContext punchIn(IChunk chunk, TFChunkGenerator generator, long duration) {
        if (duration > 0) {
            WatchdogContext context = INSTANCE.contextPool.get();
            if (context.set(chunk, generator, duration)) {
                return context;
            }
        }
        return WatchdogContext.NONE;
    }

    public static WarnTimer getWarnTimer() {
        return INSTANCE.timerPool.get();
    }

    public static long getWatchdogHangTime() {
        long time = ConfigManager.GENERAL.load().getLong(CHUNK_TIMEOUT_KEY, CRASH_TIME);
        if (time <= 0) {
            return -1L;
        }
        return Math.max(time, MIN_CRASH_TIME);
    }

    protected static void addContext(WatchdogContext context) {
        INSTANCE.contextQueue.add(context);
    }

    protected static WarnTimer createWarnTimer() {
        return new WarnTimer(ConfigManager.GENERAL.load().getLong(FEATURE_WARN_KEY, WARN_TIME));
    }
}
