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

package com.terraforged.mod.api.biome.surface;

import com.terraforged.mod.api.chunk.ChunkDelegate;
import com.terraforged.mod.api.material.state.States;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

import javax.annotation.Nullable;

public class SurfaceChunk extends ChunkDelegate {

    private int surfaceTop;
    private int surfaceBottom;

    public SurfaceChunk(IChunk chunk) {
        super(chunk);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = super.getBlockState(pos);
        if (state.getBlock() == Blocks.WATER) {
            return state;
        }
        if (state.canOcclude()) {
            return States.STONE.get();
        }
        return Blocks.AIR.defaultBlockState();
    }

    @Nullable
    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        if (pos.getY() > surfaceTop) {
            surfaceTop = pos.getY();
        }
        if (pos.getY() < surfaceBottom) {
            surfaceBottom = pos.getY();
        }
        return delegate.setBlockState(pos, state, isMoving);
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
