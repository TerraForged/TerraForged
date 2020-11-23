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

package com.terraforged.fm.template.buffer;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class PasteBuffer implements BufferIterator {

    private int index = -1;
    private BitSet placed = null;
    private boolean recordPlaced = false;

    public void reset() {
        index = -1;
    }

    public void clear() {
        index = -1;
        if (placed != null) {
            placed.clear();
        }
    }

    public void setRecording(boolean recording) {
        recordPlaced = recording;
    }

    @Override
    public boolean isEmpty() {
        return placed == null;
    }

    @Override
    public boolean next() {
        index = placed.nextSetBit(index + 1);
        return index != -1;
    }

    @Override
    public int nextIndex() {
        return index;
    }

    public void record(int i) {
        if (recordPlaced) {
            if (placed == null) {
                placed = new BitSet();
            }
            placed.set(i);
        }
    }

    public void exclude(int i) {
        if (recordPlaced) {
            if (placed != null) {
                placed.set(i, false);
            }
        }
    }

    public static void main(String[] args) {
        BitSet bitSet = new BitSet();
        for (int i = 0; i < 50; i++) {
            bitSet.set(i, true);
        }

        int index = -1;
        Set<Integer> visited = new HashSet<>();
        while (true) {
            index = bitSet.nextSetBit(index + 1);
            if (index == -1) {
                System.out.println("Done");
                return;
            }

            if (!visited.add(index)) {
                throw new RuntimeException("Duplicate " + index);
            }

            System.out.println(index);

            if (ThreadLocalRandom.current().nextInt(10) < 4) {
                bitSet.set(index, false);
                System.out.println("Removed: " + index);
            }
        }
    }
}
