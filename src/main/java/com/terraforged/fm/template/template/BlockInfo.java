package com.terraforged.fm.template.template;

import net.minecraft.block.BlockState;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;

public class BlockInfo {

    public final BlockPos pos;
    public final BlockState state;

    public BlockInfo(BlockPos pos, BlockState state) {
        this.pos = pos;
        this.state = state;
    }

    public BlockPos getPos() {
        return pos;
    }

    public BlockState getState() {
        return state;
    }

    public BlockInfo transform(Mirror mirror, Rotation rotation) {
        BlockPos pos = Template.transform(this.pos, mirror, rotation);
        BlockState state = this.state.mirror(mirror).rotate(rotation);
        return new BlockInfo(pos, state);
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
