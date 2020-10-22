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
