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
import net.minecraft.world.chunk.IChunk;

public class DeadlockException extends UncheckedException {

    private final IChunk chunk;
    private final TFChunkGenerator generator;

    protected DeadlockException(String phase, Object identity, IChunk chunk, TFChunkGenerator generator, Thread thread) {
        super(DEADLOCK, phase, identity, thread::getStackTrace);
        this.chunk = chunk;
        this.generator = generator;
        setStackTrace(thread.getStackTrace());
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    public IChunk getChunk() {
        return chunk;
    }

    public TFChunkGenerator getGenerator() {
        return generator;
    }
}
