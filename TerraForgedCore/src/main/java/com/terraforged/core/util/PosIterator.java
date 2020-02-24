/*
 *
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

package com.terraforged.core.util;

public class PosIterator {

    private final int minX;
    private final int minZ;
    private final int maxX;
    private final int maxY;
    private final int maxZ;
    private final int size;

    private int x;
    private int y;
    private int z;
    private int index = -1;

    public PosIterator(int x, int y, int z, int width, int height, int length) {
        this.x = x - 1;
        this.y = y;
        this.z = z;
        this.minX = x;
        this.minZ = z;
        this.maxX = x + width;
        this.maxY = y + height;
        this.maxZ = z + length;
        this.size = (width * height * length) - 1;
    }

    /**
     * Steps the iterator forward one position if there there is a new position within it's x/y/z bounds.
     *
     * @return true if the an increment was made, false if the maximum x,y,z coord has been reached
     */
    public boolean next() {
        if (x + 1 < maxX) {
            x += 1;
            index++;
            return true;
        }

        if (z + 1 < maxZ) {
            x = minX;
            z += 1;
            index++;
            return true;
        }

        if (y + 1 < maxY) {
            x = minX - 1;
            z = minZ;
            y += 1;
            return true;
        }

        return false;
    }

    public int size() {
        return size;
    }

    public int index() {
        return index;
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public int z() {
        return z;
    }

    /**
     * Iterates over a 2D area in the x-z planes, centered on x:z, with the given radius
     *
     * Iteration Order:
     * 1. Increments the x-axis from x - radius to x + radius (inclusive), then:
     * 2. Increments the z-axis once, resets the x-axis to x - radius, then:
     * 3. Repeats steps 1 & 2 until z reaches z + radius
     */
    public static PosIterator radius2D(int x, int z, int radius) {
        int startX = x - radius;
        int startZ = z - radius;
        int size = radius * 2 + 1;
        return new PosIterator(startX, 0, startZ, size, 0, size);
    }

    /**
     * Iterates over a 3D volume, centered on x:y:z, with the given radius
     *
     * Iteration Order:
     * 1. Increments the x-axis (starting from x - radius) up to x + radius (inclusive), then:
     * 2. Increments the z-axis once (starting from z - radius) and resets the x-axis to x - radius, then:
     * 3. Increments the y-axis once (starting from y - radius), resets the x & z axes to x - radius & z - radius
     *    respectively, then:
     * 4. Repeats steps 1-3 until y reaches y + radius
     */
    public static PosIterator radius3D(int x, int y, int z, int radius) {
        int startX = x - radius;
        int startY = y - radius;
        int startZ = z - radius;
        int size = radius * 2 + 1;
        return new PosIterator(startX, startY, startZ, size, size, size);
    }

    public static PosIterator area(int x, int z, int width, int length) {
        return new PosIterator(x, 0, z, width, 0, length);
    }

    public static PosIterator volume3D(int x, int y, int z, int width, int height, int length) {
        return new PosIterator(x, y, z, width, height, length);
    }

    public static PosIterator range2D(int minX, int minZ, int maxX, int maxZ) {
        int width = maxX - minX;
        int length = maxZ - minZ;
        return new PosIterator(minX, 0, minZ, width, 0, length);
    }

    public static PosIterator range2D(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
        int width = 1 + maxX - minX;
        int height = 1 + maxY - minY;
        int length = 1 + maxZ - minZ;
        return new PosIterator(minX, minY, minZ, width, height, length);
    }
}