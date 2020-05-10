package com.terraforged.api.material.state;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.tags.Tag;

import java.util.function.Predicate;

public class StateTagPredicate implements Predicate<BlockState> {

    private final Tag<Block> tag;

    public StateTagPredicate(Tag<Block> tag) {
        this.tag = tag;
    }

    @Override
    public boolean test(BlockState state) {
        return tag.contains(state.getBlock());
    }
}
