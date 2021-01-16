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

import java.util.concurrent.locks.StampedLock;

public class ContextQueue {

    private static final WatchdogContext[] EMPTY = new WatchdogContext[0];

    private volatile WatchdogContext[] queue = EMPTY;
    private final StampedLock lock = new StampedLock();

    public void add(WatchdogContext context) {
        long stamp = lock.writeLock();
        try {
            WatchdogContext[] queue = this.queue;
            int size = queue.length;

            WatchdogContext[] next = new WatchdogContext[size + 1];
            System.arraycopy(queue, 0, next, 0, size);
            next[size] = context;

            this.queue = next;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    public WatchdogContext[] get() {
        long opt = lock.tryOptimisticRead();
        WatchdogContext[] queue = this.queue;
        if (!lock.validate(opt)) {
            long stamp = lock.readLock();
            try {
                queue = this.queue;
            } finally {
                lock.unlockRead(stamp);
            }
        }
        return queue;
    }
}
