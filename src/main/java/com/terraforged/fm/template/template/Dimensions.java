package com.terraforged.fm.template.template;

import net.minecraft.util.math.BlockPos;

public class Dimensions {

    public final BlockPos min;
    public final BlockPos max;

    public Dimensions(BlockPos min, BlockPos max) {
        this.min = min;
        this.max = max;
    }
}
