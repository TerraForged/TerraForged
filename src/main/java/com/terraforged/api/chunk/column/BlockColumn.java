package com.terraforged.api.chunk.column;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.FluidState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;

public class BlockColumn implements IBlockReader {

    private int height = 0;
    private BlockState[] column = null;

    public BlockColumn withCapacity(int size) {
        if (column == null || column.length < size) {
            column = new BlockState[size];
            height = size;
        }
        return this;
    }

    public void set(int i, BlockState state) {
        column[i] = state;
    }

    @Override
    public TileEntity getTileEntity(BlockPos pos) {
        return null;
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        int y = pos.getY();
        return y >= 0 && y < height ? column[y] : Blocks.AIR.getDefaultState();
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return this.getBlockState(pos).getFluidState();
    }
}
