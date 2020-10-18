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
