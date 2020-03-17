package com.terraforged.mod.feature.placement;

import net.minecraft.util.math.BlockPos;

public interface PosGenerator {

    boolean generate(BlockPos.Mutable next);
}
