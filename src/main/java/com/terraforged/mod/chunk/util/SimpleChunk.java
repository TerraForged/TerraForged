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

import com.terraforged.api.material.state.States;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.IChunk;

public class SimpleChunk extends FastChunk {

    protected SimpleChunk(ChunkPrimer delegate) {
        super(delegate);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        BlockState state = super.getBlockState(pos);
        if (state.getBlock() == Blocks.WATER) {
            return state;
        }
        if (state.isSolid()) {
            return States.STONE.get();
        }
        return Blocks.AIR.getDefaultState();
    }

    public static IChunk wrap(IChunk chunk) {
        if (chunk instanceof ChunkPrimer) {
            return new SimpleChunk((ChunkPrimer) chunk);
        }
        if (chunk instanceof FastChunk) {
            return wrap(((FastChunk) chunk).getDelegate());
        }
        return chunk;
    }
}
