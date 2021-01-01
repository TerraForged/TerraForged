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

package com.terraforged.mod.server.command.search;

import com.terraforged.engine.concurrent.cache.SafeCloseable;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class Search implements Supplier<BlockPos>, SafeCloseable {

    protected static final int MIN_RADIUS = 128;
    protected static final int MAX_RADIUS = 24000;

    private final BlockPos center;
    private final int minRadius;
    private final int maxRadius;
    private final double minRadius2;

    public Search(BlockPos center) {
        this(center, MIN_RADIUS);
    }

    public Search(BlockPos center, int minRadius) {
        this(center, minRadius, MAX_RADIUS);
    }

    public Search(BlockPos center, int minRadius, int maxRadius) {
        this.center = center;
        this.minRadius = minRadius;
        this.minRadius2 = minRadius * minRadius;
        this.maxRadius = Math.min(maxRadius, MAX_RADIUS);
    }

    public int getMinRadius() {
        return minRadius;
    }

    public int getSpacing() {
        return 16;
    }

    @Override
    public void close() {

    }

    @Override
    public BlockPos get() {
        int radius = maxRadius;

        int x = 0;
        int z = 0;
        int dx = 0;
        int dz = -1;
        int size = radius + 1 + radius;
        long max = (long) size * (long) size;
        long timeOut = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30);
        BlockPos.Mutable pos = new BlockPos.Mutable();

        for (long i = 0; i < max; i++) {
            if (System.currentTimeMillis() > timeOut) {
                break;
            }

            if ((-radius <= x) && (x <= radius) && (-radius <= z) && (z <= radius)) {
                pos.setPos(center.getX() + (x * getSpacing()), center.getY(), center.getZ() + (z * getSpacing()));
                if (minRadius2 == 0 || center.distanceSq(pos) >= minRadius2) {
                    if (test(pos)) {
                        return success(pos);
                    }
                }
            }

            if ((x == z) || ((x < 0) && (x == -z)) || ((x > 0) && (x == 1 - z))) {
                size = dx;
                dx = -dz;
                dz = size;
            }

            x += dx;
            z += dz;
        }

        return fail(BlockPos.ZERO);
    }

    public abstract boolean test(BlockPos pos);

    public BlockPos success(BlockPos.Mutable pos) {
        return pos.toImmutable();
    }

    public BlockPos fail(BlockPos pos) {
        return pos;
    }
}
