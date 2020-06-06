package com.terraforged.command.search;

import net.minecraft.util.math.BlockPos;

public class BothSearchTask extends Search {

    private final int spacing;
    private final Search a;
    private final Search b;

    public BothSearchTask(BlockPos center, Search a, Search b) {
        super(center, Math.min(a.getMinRadius(), b.getMinRadius()));
        this.spacing = Math.min(a.getSpacing(), b.getSpacing());
        this.a = a;
        this.b = b;
    }

    @Override
    public int getSpacing() {
        return spacing;
    }

    @Override
    public boolean test(BlockPos pos) {
        return a.test(pos) && b.test(pos);
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        return a.success(pos);
    }
}
