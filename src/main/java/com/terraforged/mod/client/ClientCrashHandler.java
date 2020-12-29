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

package com.terraforged.mod.client;

import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.util.crash.CrashHandler;
import com.terraforged.mod.util.crash.CrashReportBuilder;
import com.terraforged.mod.util.crash.WorldGenException;
import net.minecraft.client.Minecraft;
import net.minecraft.crash.CrashReport;
import net.minecraft.world.chunk.IChunk;

import java.util.concurrent.locks.StampedLock;

public class ClientCrashHandler implements CrashHandler {

    private final StampedLock lock = new StampedLock();

    @Override
    public void crash(IChunk chunk, TFChunkGenerator generator, WorldGenException e) {
        final long stamp = lock.tryWriteLock();
        if (!lock.validate(stamp)) {
            return;
        }

        try {
            CrashReport report = CrashReportBuilder.buildCrashReport(chunk, generator, e);
            Minecraft.getInstance().crashed(report);
        } finally {
            lock.unlockWrite(stamp);
        }
    }
}
