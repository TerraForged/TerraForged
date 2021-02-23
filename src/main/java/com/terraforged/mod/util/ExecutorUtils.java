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

package com.terraforged.mod.util;

import net.minecraft.crash.ReportedException;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Bootstrap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicInteger;

public class ExecutorUtils {

    private static final Logger LOGGER = LogManager.getLogger();
    private static final AtomicInteger NEXT_SERVER_WORKER_ID = new AtomicInteger();

    public static ExecutorService create(String serviceName) {
        int i = Runtime.getRuntime().availableProcessors();

        if (i < 4) {
            return null;
        }

        int count = Math.round(i * 1.5F);
        return new ForkJoinPool(count, pool -> {
            ForkJoinWorkerThread thread = createThread(pool);
            thread.setName("Worker-" + serviceName + "-" + NEXT_SERVER_WORKER_ID.getAndIncrement());
            return thread;
        }, ExecutorUtils::printException, true);
    }

    private static ForkJoinWorkerThread createThread(ForkJoinPool pool) {
        return new ForkJoinWorkerThread(pool) {
            @Override
            protected void onTermination(Throwable t) {
                if (t != null) {
                    LOGGER.warn("{} died", this.getName(), t);
                } else {
                    LOGGER.debug("{} shutdown", this.getName());
                }
                super.onTermination(t);
            }
        };
    }

    private static void printException(Thread thread, Throwable throwable) {
        Util.pauseDevMode(throwable);

        if (throwable instanceof CompletionException) {
            throwable = throwable.getCause();
        }

        if (throwable instanceof ReportedException) {
            Bootstrap.printToSYSOUT(((ReportedException)throwable).getCrashReport().getCompleteReport());
            System.exit(-1);
        }

        LOGGER.error(String.format("Caught exception in thread %s", thread), throwable);
    }
}
