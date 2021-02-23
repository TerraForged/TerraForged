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

package com.terraforged.mod.util.quadsearch;

import com.terraforged.engine.concurrent.cache.SafeCloseable;
import com.terraforged.mod.Log;
import com.terraforged.noise.util.Vec2i;

import java.util.Comparator;
import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeUnit;

public class QuadSearch {

    private static final Vec2i[] QUADS = {
            new Vec2i(+1, +1),
            new Vec2i(+1, -1),
            new Vec2i(-1, +1),
            new Vec2i(-1, -1),
    };

    public static <T> T search(int x, int z, int radius, int stepSize, Search<T> search, SearchContext context) {
        final long start = System.nanoTime();
        try {
            return search(x, z, radius, stepSize, 0, 1, search, context.startTask());
        } finally {
            long duration = System.nanoTime() - start;
            Log.debug("Search completed in {}ms", TimeUnit.NANOSECONDS.toMillis(duration));
        }
    }

    public static <T> T asyncSearch(int x, int z, int radius, int stepSize, Search<T> search, SearchContext context) {
        return asyncSearch(x, z, radius, stepSize, Runtime.getRuntime().availableProcessors(), search, context);
    }

    @SuppressWarnings("unchecked")
    public static <T> T asyncSearch(int x, int z, int radius, int stepSize, int workers, Search<T> search, SearchContext context) {
        if (workers <= 1) {
            return search(x, z, radius, stepSize, search, context.startTask());
        }

        final long start = System.nanoTime();
        try {
            ForkJoinTask<T>[] tasks = new ForkJoinTask[workers];
            for (int i = 0; i < workers; i++) {
                final int workerId = i;
                final SearchContext taskContext = context.startTask();
                Callable<T> task = () -> search(x, z, radius, stepSize, workerId, workers, search, taskContext);
                tasks[workerId] = ForkJoinPool.commonPool().submit(task);
            }

            return waitOnResults(tasks, search, context);
        } finally {
            long duration = System.nanoTime() - start;
            Log.debug("Async search completed in {}ms", TimeUnit.NANOSECONDS.toMillis(duration));
        }
    }

    private static <T> T search(int x, int z, int searchRadius, int stepSize, int worker, int workers, Search<T> search, SearchContext context) {
        // Make sure thread local resources are disposed on exit
        try (SafeCloseable ignored1 = search; SafeCloseable ignored2 = context) {
            // Keeps track of tasks so that they can be evenly distributed across threads
            int taskCounter = 0;

            // Increment the radius to create concentric rings
            for (int radius = 0; radius <= searchRadius; radius++) {

                // Iterate along the edge of the ring from 0 to radius
                for (int r = 0; r <= radius; r++) {
                    // Exit if out of time or past the nearest successful result
                    if (context.isComplete(radius)) {
                        break;
                    }

                    int task1 = taskCounter++;
                    int task2 = r == radius ? -1 : taskCounter++; // Avoid checking duplicate pos at x=radius,z=radius

                    // Search each quad segment of the ring
                    T result = searchQuads(x, z, r, radius, stepSize, worker, workers, task1, task2, search, context);

                    // The lower r is the closer the position is to x,z so exit early on first nonnull result
                    if (result != null) {
                        // Let other threads know which ring we completed on so they can exit early if they've
                        // already searched past it.
                        context.submit(radius);
                        return result;
                    }
                }
            }
        }
        return null;
    }

    private static <T> T searchQuads(int x, int z, int r, int radius, int spacing, int worker, int workers, int task1, int task2, Search<T> search, SearchContext context) {
        // Scaled radii
        final int rf = r * spacing;
        final int radiusf = radius * spacing;

        T result = null;

        // Test the ring segments for each quad, accumulating the result in-case we find multiple matches
        for (Vec2i dir : QUADS) {
            // Exit if out of time or past the nearest successful result.
            if (context.isComplete(r)) {
                break;
            }

            // Offset by one step in the negative axes to avoid checking the zero coords twice
            int startX = x + Math.min(0, dir.x * spacing);
            int startZ = z + Math.min(0, dir.y * spacing);

            // Incrementing x along the ring's edge where z=radiusf
            if (isCurrentThreadsTask(task1, worker, workers)) {
                int px = startX + dir.x * rf;
                int maxZ = startZ + (dir.y * radiusf);
                if (search.test(px, maxZ)) {
                    task1 = -1;
                    result = getFirstResult(result, search.result(), search);
                }
            }

            // Incrementing z along the ring's edge where x=radiusf
            if (isCurrentThreadsTask(task2, worker, workers)) {
                int pz = startZ + dir.y * rf;
                int maxX = startX + (dir.x * radiusf);
                if (search.test(maxX, pz)) {
                    task2 = -1;
                    result = getFirstResult(result, search.result(), search);
                }
            }

            // Exit early if no more tasks to process
            if (task1 == -1 && task2 == -1) {
                break;
            }
        }

        return result;
    }

    private static boolean isCurrentThreadsTask(int taskId, int workerId, int workerCount) {
        return taskId != -1 && workerCount > 1 && (taskId % workerCount) == workerId;
    }

    private static <T> T waitOnResults(ForkJoinTask<T>[] tasks, Comparator<T> comparator, SearchContext context) {
        T result = null;

        while (context.hasTasks() && !context.hasTimedOut()) {
            for (ForkJoinTask<T> task : tasks) {
                if (task.isDone()) {
                    result = getFirstResult(result, task.join(), comparator);
                }
            }
        }

        return result;
    }

    private static <T> T getFirstResult(T current, T value, Comparator<T> comparator) {
        if (current == null) {
            return value;
        }
        if (value == null) {
            return current;
        }
        if (comparator.compare(value, current) <= 0) {
            return value;
        }
        return current;
    }
}
