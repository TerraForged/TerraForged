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

package com.terraforged.mod.featuremanager.template.buffer;

import java.util.BitSet;

public class BufferBitSet {

    private int minX;
    private int minY;
    private int minZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int sizeXZ;
    private BitSet bitSet;

    public void set(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.sizeX = Math.max(x1, x2) - minX;
        this.sizeY = Math.max(y1, y2) - minY;
        this.sizeZ = Math.max(z1, z2) - minZ;
        this.sizeXZ = sizeX * sizeZ;
        int size = sizeX * sizeY * sizeZ;
        if (bitSet == null || bitSet.length() < size) {
            bitSet = new BitSet(size);
        } else {
            bitSet.clear();
        }
    }

    public void clear() {
        if (bitSet != null) {
            bitSet.clear();
        }
    }

    public void set(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        bitSet.set(index);
    }

    public void unset(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        bitSet.set(index, false);
    }

    public boolean test(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        if (index < 0 || index >= bitSet.length()) {
            return false;
        }
        return bitSet.get(index);
    }

    private int indexOf(int x, int y, int z) {
        return (y * sizeXZ) + (z * sizeX) + x;
    }
}
