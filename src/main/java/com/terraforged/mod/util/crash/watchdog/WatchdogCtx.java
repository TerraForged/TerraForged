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
import com.terraforged.mod.featuremanager.util.identity.Identifier;
import net.minecraft.world.chunk.IChunk;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

class WatchdogCtx implements WatchdogContext {

    protected static final List<WatchdogCtx> CONTEXTS = new CopyOnWriteArrayList<>();

    private volatile String phase = "";
    private volatile Object identifier = null;
    private volatile IChunk chunk = null;
    private volatile TFChunkGenerator generator = null;

    private volatile long start = 0L;
    private volatile long timeout = 0L;
    private final AtomicReference<Thread> threadRef = new AtomicReference<>();

    protected WatchdogCtx() {
        CONTEXTS.add(this);
    }

    protected boolean set(IChunk chunk, TFChunkGenerator generator, long now, long duration) {
        if (threadRef.compareAndSet(null, Thread.currentThread())) {
            this.phase = "";
            this.identifier = null;
            this.chunk = chunk;
            this.generator = generator;
            this.start = now;
            this.timeout = now + duration;
            return true;
        }
        return false;
    }

    protected void check(long now) throws DeadlockException {
        Thread thread = threadRef.getAndSet(null);
        if (thread == null) {
            return;
        }

        long timeout = this.timeout;
        if (timeout == 0L || now < timeout) {
            threadRef.compareAndSet(null, thread);
            return;
        }

        try {
            String phase = this.phase;
            Object identity = this.identifier;
            IChunk chunk = this.chunk;
            TFChunkGenerator generator = this.generator;
            long time = now - this.start;
            throw new DeadlockException(phase, identity, time, chunk, generator, thread);
        } finally {
            close();
        }
    }

    @Override
    public void pushPhase(String phase) {
        this.phase = phase;
    }

    @Override
    public void pushIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public void pushIdentifier(Identifier identifier) {
        this.identifier = identifier;
    }

    @Override
    public void close() {
        threadRef.set(null);
        phase = "";
        identifier = null;
        chunk = null;
        generator = null;
    }
}
