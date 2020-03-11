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

package com.terraforged.api.chunk.surface;

import com.terraforged.api.chunk.ChunkDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.Chunk;

public class ChunkSurfaceBuffer implements ChunkDelegate {

    private int surfaceTop;
    private int surfaceBottom;
    private final Chunk delegate;

    public ChunkSurfaceBuffer(Chunk chunk) {
        this.delegate = chunk;
    }

    @Override
    public Chunk getDelegate() {
        return delegate;
    }

    //@Nullable
    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        if (pos.getY() > surfaceTop) {
            surfaceTop = pos.getY();
        }
        if (pos.getY() < surfaceBottom) {
            surfaceBottom = pos.getY();
        }
        return getDelegate().setBlockState(pos, state, isMoving);
    }

    public int getSurfaceTop() {
        return surfaceTop;
    }

    public int getSurfaceBottom() {
        return surfaceBottom;
    }

    public void setSurfaceLevel(int y) {
        surfaceTop = y;
        surfaceBottom = y;
    }
}
