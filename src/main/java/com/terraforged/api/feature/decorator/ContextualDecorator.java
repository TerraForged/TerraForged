package com.terraforged.api.feature.decorator;

import com.mojang.serialization.Codec;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public abstract class ContextualDecorator<T extends IPlacementConfig> extends Placement<T> {
    
    public ContextualDecorator(Codec<T> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> func_241857_a(WorldDecoratingHelper helper, Random random, T config, BlockPos pos) {
        DecorationContext context = DecorationHelper.getContext(helper);
        if (context == null) {
            new UnsupportedOperationException().printStackTrace();
            return Stream.empty();
        }
        return getPositions(helper, context, random, config, pos);
    }
    
    protected abstract Stream<BlockPos> getPositions(WorldDecoratingHelper helper, DecorationContext context, Random random, T config, BlockPos pos);
}
