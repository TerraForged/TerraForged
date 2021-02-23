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

import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.pool.ObjectPool;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class DummyBlockReader implements IBlockReader {

    private static final ObjectPool<DummyBlockReader> pool = new ObjectPool<>(10, DummyBlockReader::new);

    private BlockState state;
    private FluidState fluid;

    public DummyBlockReader set(BlockState state) {
        return set(state, Fluids.EMPTY.getDefaultState());
    }

    public DummyBlockReader set(FluidState fluid) {
        return set(Blocks.AIR.getDefaultState(), fluid);
    }

    public DummyBlockReader set(BlockState state, FluidState fluid) {
        this.state = state;
        this.fluid = fluid;
        return this;
    }

    @Nullable
    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return state;
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return fluid;
    }

    public static Resource<DummyBlockReader> pooled() {
        return pool.get();
    }
}
