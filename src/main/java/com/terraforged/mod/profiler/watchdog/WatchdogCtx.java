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
import com.terraforged.mod.profiler.timings.TimingStack;
import com.terraforged.mod.profiler.timings.Top3TimingStack;
import net.minecraft.world.chunk.IChunk;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.StampedLock;

class WatchdogCtx implements WatchdogContext {

    protected static final List<WatchdogCtx> CONTEXTS = new CopyOnWriteArrayList<>();

    private String phase = "";
    private IChunk chunk = null;
    private Object identifier = null;
    private TFChunkGenerator generator = null;

    private long start = 0L;
    private long timeout = 0L;
    private long itemStart = 0L;
    private Thread thread = null;
    private final StampedLock lock = new StampedLock();
    private final TimingStack stack = new Top3TimingStack();

    protected WatchdogCtx() {
        CONTEXTS.add(this);
    }

    @Override
    public void pushPhase(String phase) {
        long stamp = lock.writeLock();
        try {
            this.phase = phase;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void pushIdentifier(Object identifier, long timeStamp) {
        long stamp = lock.writeLock();
        try {
            this.identifier = identifier;
            this.itemStart = timeStamp;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    @Override
    public void pushTime(String type, Object o, long time) {
        this.stack.push(type, o, time);
    }

    @Override
    public void close() {
        long read = lock.tryOptimisticRead();
        boolean closed = thread == null;
        if (lock.validate(read) && closed) {
            return;
        }

        long stamp = lock.writeLock();
        try {
            phase = "";
            start = 0L;
            timeout = 0L;
            itemStart = 0L;
            chunk = null;
            thread = null;
            generator = null;
            identifier = null;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    boolean set(IChunk chunk, TFChunkGenerator generator, long duration) {
        long stamp = lock.writeLock();
        try {
            if (thread != null) {
                return false;
            }
            long now = System.currentTimeMillis();
            this.phase = "Start";
            this.start = now;
            this.chunk = chunk;
            this.identifier = null;
            this.generator = generator;
            this.timeout = now + duration;
            this.itemStart = 0L;
            this.thread = Thread.currentThread();
            this.stack.reset();
            return true;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    void check(long now) throws DeadlockException {
        if (optimisticCheck(now)) {
            return;
        }
        // fall back to a full read lock if optimistic failed
        lockedCheck(now);
    }

    /**
     * Attempts to optimistically perform a deadlock check.
     * This has slightly less overhead than simply acquiring the read lock.
     *
     * Returns false if data was invalidated after read
     * Returns true if data was validated after read & no deadlock detected
     * Throws a DeadlockException if the data was valid & a deadlock was detected
     */
    private boolean optimisticCheck(long now) throws DeadlockException {
        long stamp = lock.tryOptimisticRead();
        DeadlockException deadlock = getDeadlock(now);
        if (lock.validate(stamp)) {
            if (deadlock == null) {
                return true;
            }
            throw deadlock;
        }
        return false;
    }

    private void lockedCheck(long now) throws DeadlockException {
        long stamp = lock.readLock();
        try {
            DeadlockException deadlock = getDeadlock(now);
            if (deadlock == null) {
                return;
            }
            throw deadlock;
        } finally {
            lock.unlockRead(stamp);
        }
    }

    private DeadlockException getDeadlock(long now) {
        if (thread == null) {
            return null;
        }

        if (timeout == 0L || now <= timeout) {
            return null;
        }

        long totalTime = now - start;
        long itemTime = now - itemStart;
        return new DeadlockException(phase, identifier, totalTime, itemTime, stack.copy(), chunk, generator, thread);
    }
}
