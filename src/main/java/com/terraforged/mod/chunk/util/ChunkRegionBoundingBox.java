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

import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;

public class ChunkRegionBoundingBox extends MutableBoundingBox {

    private static final int INCLUSIVE_SIZE = 15;
    private static final String ERROR_MESSAGE = "Attempted to set the world-gen region to an unsafe size! Chunks: ";

    private final int x1, z1, x2, z2;
    private final int rx1, rz1, rx2, rz2;

    public ChunkRegionBoundingBox(int chunkX, int chunkZ, int chunkRadius) {
        super(chunkX << 4, chunkZ << 4, (chunkX << 4) + INCLUSIVE_SIZE, (chunkZ << 4) + INCLUSIVE_SIZE);
        this.x1 = minX;
        this.z1 = minZ;
        this.x2 = maxX;
        this.z2 = maxZ;
        this.rx1 = (chunkX - chunkRadius) << 4;
        this.rz1 = (chunkZ - chunkRadius) << 4;
        this.rx2 = ((chunkX + chunkRadius) << 4) + INCLUSIVE_SIZE;
        this.rz2 = ((chunkZ + chunkRadius) << 4) + INCLUSIVE_SIZE;
    }

    public ChunkRegionBoundingBox reset() {
        minX = x1;
        minZ = z1;
        maxX = x2;
        maxZ = z2;
        minY = 1;
        maxY = 512;
        return this;
    }

    @Override
    public void expandTo(MutableBoundingBox other) {
        super.expandTo(other);
        validate();
    }

    @Override
    public void offset(int x, int y, int z) {
        super.offset(x, y, z);
        validate();
    }

    @Override
    public void func_236989_a_(Vector3i vec) {
        super.func_236989_a_(vec);
        validate();
    }

    @Override
    public String toString() {
        return "BoundingBox{"
                + "minX=" + minX + ", minZ=" + minZ + ", maxX=" + maxX + ", maxZ=" + maxZ
                + ", sizeChunks=" + getSizeChunks()
                + "}";
    }

    private String getSizeChunks() {
        int chunkSizeX = (maxX >> 4) - (minX >> 4);
        int chunkSizeZ = (maxZ >> 4) - (minZ >> 4);
        return chunkSizeX + "x" + chunkSizeZ;
    }

    private void validate() {
        if (!contains(minX, minZ, rx1, rz1, rx2, rz2) || !contains(maxX, maxZ, rx1, rz1, rx2, rz2)) {
            throw new UnsupportedOperationException(ERROR_MESSAGE + getSizeChunks());
        }
    }

    public static boolean contains(int x, int z, MutableBoundingBox bounds) {
        return contains(x, z, bounds.minX, bounds.minZ, bounds.maxX, bounds.maxZ);
    }

    public static boolean contains(int x, int z, int minX, int minZ, int maxX, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }
}
