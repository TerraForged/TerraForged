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

package com.terraforged.mod.command.task;

import me.dags.noise.util.Vec2i;
import net.minecraft.util.math.BlockPos;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public abstract class FindTask implements Supplier<Vec2i> {

    private static final int RADIUS = 5000;

    private final BlockPos center;
    private final int minRadius;

    private final long timeout;

    protected FindTask(BlockPos center, int minRadius) {
        this.center = center;
        this.minRadius = minRadius;
        this.timeout = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(20);
    }

    @Override
    public Vec2i get() {
        try {
            int x = 0;
            int y = 0;
            int dx = 0;
            int dy = -1;
            int radius = RADIUS;
            int size = radius + 1 + radius;
            int max = size * size;

            int minSize = minRadius + 1 + minRadius;
            int min = minSize * minSize;

            for (int i = 0; i < max; i++) {
                if (i % 100 == 0) {
                    if (System.currentTimeMillis() > timeout) {
                        break;
                    }
                }

                if ((-radius <= x) && (x <= radius) && (-radius <= y) && (y <= radius)) {
                    int distX = transformCoord(x);
                    int distY = transformCoord(y);
                    int dist2 = distX * distX + distY * distY;
                    if (dist2 > min) {
                        int posX = center.getX() + distX;
                        int posZ = center.getZ() + distY;
                        if (search(posX, posZ)) {
                            return new Vec2i(posX, posZ);
                        }
                    }
                }

                if ((x == y) || ((x < 0) && (x == -y)) || ((x > 0) && (x == 1 - y))) {
                    size = dx;
                    dx = -dy;
                    dy = size;
                }

                x += dx;
                y += dy;
            }
            return new Vec2i(0, 0);
        } catch (Throwable t) {
            t.printStackTrace();
            return new Vec2i(0, 0);
        }
    }

    protected abstract int transformCoord(int coord);

    /**
     * Performs the search at the given x,z coordinates and returns true if a result is found
     *
     * @param x the x block coordinate
     * @param z the z block coordinate
     * @return the search result
     */
    protected abstract boolean search(int x, int z);
}
