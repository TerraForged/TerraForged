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

package com.terraforged.mod.chunk.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.world.chunk.ChunkSection;

import java.util.Arrays;
import java.util.concurrent.locks.StampedLock;

public class FastSection extends ChunkSection {

    private final StampedLock lock = new StampedLock();
    private final BlockState[] states = new BlockState[16 * 16 * 16];

    private volatile long writeLock = 0L;

    public FastSection(int sectionId) {
        super(sectionId);
    }

    public FastSection init() {
        Arrays.fill(states, Blocks.AIR.getDefaultState());
        return this;
    }

    @Override
    public void lock() {
        long stamp = lock.writeLock();
        writeLock = stamp;
    }

    @Override
    public void unlock() {
        long stamp = writeLock;
        writeLock = -1L;
        lock.unlockWrite(stamp);
    }

    @Override
    public BlockState getBlockState(int x, int y, int z) {
        return states[index(x, y, z)];
    }

    @Override
    public FluidState getFluidState(int x, int y, int z) {
        return getBlockState(x, y, z).getFluidState();
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState state) {
        int index = index(x, y, z);
        BlockState old = states[index];
        states[index] = state;
        return old;
    }

    @Override
    public BlockState setBlockState(int x, int y, int z, BlockState state, boolean useLocks) {
        if (useLocks) {
            lock();
        }

        int index = index(x, y, z);
        BlockState old = states[index];
        states[index] = state;

        if (useLocks) {
            unlock();
        }

        return old;
    }

    public ChunkSection compile() {
        ChunkSection section = new ChunkSection(getYLocation());
        for (int dy = 0; dy < 16; dy++) {
            for (int dz = 0; dz < 16; dz++) {
                for (int dx = 0; dx < 16; dx++) {
                    section.setBlockState(dx, dy, dz, states[index(dx, dy, dz)], false);
                }
            }
        }
        return section;
    }

    private static int index(int x, int y, int z) {
        return (y << 5) + (z << 4) + x;
    }
}
