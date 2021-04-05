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

import com.terraforged.mod.Log;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.gen.feature.structure.StructureStart;

public class ChunkRegionBoundingBox extends MutableBoundingBox {

    private static final int INCLUSIVE_SIZE = 15;
    private static final String ERROR_MESSAGE = "Structure {} attempted to change the world-gen region bounds to an unsafe location/size. Original: {}, Altered: {}";

    private final int boundX0, boundZ1, boundX1, boundZ2;
    private final int radiusX0, radiusZ0, radiusX1, radiusZ1;

    private String structure = "unknown";

    public ChunkRegionBoundingBox(int chunkX, int chunkZ, int chunkRadius) {
        super(chunkX << 4, chunkZ << 4, (chunkX << 4) + INCLUSIVE_SIZE, (chunkZ << 4) + INCLUSIVE_SIZE);
        this.boundX0 = x0;
        this.boundZ1 = z0;
        this.boundX1 = x1;
        this.boundZ2 = z1;
        this.radiusX0 = (chunkX - chunkRadius) << 4;
        this.radiusZ0 = (chunkZ - chunkRadius) << 4;
        this.radiusX1 = ((chunkX + chunkRadius) << 4) + INCLUSIVE_SIZE;
        this.radiusZ1 = ((chunkZ + chunkRadius) << 4) + INCLUSIVE_SIZE;
    }

    public ChunkRegionBoundingBox init(StructureStart<?> start) {
        return init(start.getFeature().getFeatureName());
    }

    public ChunkRegionBoundingBox init(String name) {
        x0 = boundX0;
        z0 = boundZ1;
        x1 = boundX1;
        z1 = boundZ2;
        y0 = 1;
        y1 = 512;
        structure = name;
        return this;
    }

    @Override
    public void expand(MutableBoundingBox other) {
        super.expand(other);
        validate();
    }

    @Override
    public void move(int x, int y, int z) {
        super.move(x, y, z);
        validate();
    }

    @Override
    public void move(Vector3i vec) {
        super.move(vec);
        validate();
    }

    @Override
    public String toString() {
        return "BoundingBox{"
                + "minX=" + x0 + ", minZ=" + z0 + ", maxX=" + x1 + ", maxZ=" + z1
                + ", sizeChunks=" + getSizeChunks()
                + "}";
    }

    private String getSizeChunks() {
        int chunkSizeX = (x1 >> 4) - (x0 >> 4);
        int chunkSizeZ = (z1 >> 4) - (z0 >> 4);
        return chunkSizeX + "x" + chunkSizeZ;
    }

    private void validate() {
        if (!contains(x0, z0, radiusX0, radiusZ0, radiusX1, radiusZ1) || !contains(x1, z1, radiusX0, radiusZ0, radiusX1, radiusZ1)) {
            String from = toString(radiusX0, radiusZ0, radiusX1, radiusZ1);
            String to = toString(x0, z0, x1, z1);
            Log.warn(ERROR_MESSAGE, structure, from, to);
        }
    }

    public static boolean contains(int x, int z, MutableBoundingBox bounds) {
        return contains(x, z, bounds.x0, bounds.z0, bounds.x1, bounds.z1);
    }

    public static boolean contains(int x, int z, int minX, int minZ, int maxX, int maxZ) {
        return x >= minX && x <= maxX && z >= minZ && z <= maxZ;
    }

    public static String toString(int minX, int minZ, int maxX, int maxZ) {
        return "Bounds{minX=" + minX + ",minZ=" + minZ + ",maxX=" + maxX + ",maxZ=" + maxZ + "}";
    }
}
