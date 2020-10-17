package com.terraforged.api.feature.decorator.filter;

import com.terraforged.api.feature.decorator.ContextualDecorator;
import com.terraforged.api.feature.decorator.DecorationContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.ConfiguredPlacement;

import java.util.Random;
import java.util.stream.Stream;

public class FilterDecorator extends ContextualDecorator<FilterDecoratorConfig> {

    public static final FilterDecorator INSTANCE = new FilterDecorator();

    private FilterDecorator() {
        super(FilterDecoratorConfig.CODEC);
        setRegistryName("terraforged", "filter");
    }

    @Override
    protected Stream<BlockPos> getPositions(WorldDecoratingHelper helper, DecorationContext context, Random random, FilterDecoratorConfig config, BlockPos pos) {
        return config.placement.func_242876_a(helper, random, pos).filter(p -> config.filter.test(context, p));
    }
}
