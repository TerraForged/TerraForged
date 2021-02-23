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

package com.terraforged.mod.chunk.util;

import net.minecraft.util.math.BlockPos;

import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ChunkBitSet implements Spliterator<BlockPos.Mutable> {

    private int index = -1;
    private int count = 0;
    private long l0, l1, l2, l3;

    private int offsetX;
    private int offsetY;
    private int offsetZ;
    private final BlockPos.Mutable pos = new BlockPos.Mutable();

    public void set(int x, int z) {
        set(z + (x << 4));
    }

    public void set(int index) {
        int i = index >> 6;
        if (i == 0) {
            l0 |= (1L << index);
            count++;
        } else if (i == 1) {
            l1 |= (1L << index);
            count++;
        } else if (i == 2) {
            l2 |= (1L << index);
            count++;
        } else if (i == 3) {
            l3 |= (1L << index);
            count++;
        }
    }

    public boolean get(int x, int z) {
        return get(z + (x << 4));
    }

    public boolean get(int index) {
        int i = index >> 6;
        return (getSection(i) & (1L << index)) != 0;
    }

    public int next(int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException("fromIndex < 0: " + index);
        }

        int sectionIndex = index >> 6;
        if (sectionIndex > 3) {
            return -1;
        }

        long section = getSection(sectionIndex) & (0xffffffffffffffffL << index);
        while (true) {
            if (section != 0) {
                return (sectionIndex * 64) + Long.numberOfTrailingZeros(section);
            }

            if (++sectionIndex == 4) {
                return -1;
            }

            section = getSection(sectionIndex);
        }
    }

    public Stream<BlockPos.Mutable> stream() {
        return stream(0, 0, 0);
    }

    public Stream<BlockPos.Mutable> stream(int x, int y, int z) {
        index = -1;
        offsetX = x;
        offsetY = y;
        offsetZ = z;
        return StreamSupport.stream(this, false);
    }

    @Override
    public boolean tryAdvance(Consumer<? super BlockPos.Mutable> action) {
        index = next(index + 1);
        if (index == -1) {
            return false;
        } else {
            int x = index >> 4;
            int z = index - (x << 4);
            action.accept(pos.setPos(offsetX + x, offsetY, offsetZ + z));
            return true;
        }
    }

    @Override
    public Spliterator<BlockPos.Mutable> trySplit() {
        return this;
    }

    @Override
    public long estimateSize() {
        return count;
    }

    @Override
    public int characteristics() {
        return Spliterator.DISTINCT | Spliterator.ORDERED;
    }

    private long getSection(int i) {
        if (i == 0) {
            return l0;
        } else if (i == 1) {
            return l1;
        } else if (i == 2) {
            return l2;
        } else if (i == 3) {
            return l3;
        }
        return 0;
    }
}
