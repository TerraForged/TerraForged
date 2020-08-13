package com.terraforged.mod.feature.context;

import com.terraforged.core.concurrent.Resource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public class ContextSelectorFeature extends Feature<ContextSelectorConfig> {

    public static final ContextSelectorFeature INSTANCE = new ContextSelectorFeature();

    public ContextSelectorFeature() {
        super(ContextSelectorConfig.CODEC);
        setRegistryName("terraforged", "context_selector");
    }

    @Override
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, ContextSelectorConfig config) {
        try (Resource<ChanceContext> item = ChanceContext.pooled(world, generator)) {
            if (item == null) {
                return false;
            }

            ChanceContext context = item.get();
            context.setPos(pos);
            context.init(config.features.size());
            for (int i = 0; i < config.features.size(); i++) {
                ContextualFeature feature = config.features.get(i);
                float chance = feature.getChance(pos, context);
                context.record(i, chance);
            }

            int index = context.nextIndex(random);
            if (index > -1) {
                return config.features.get(index).feature.func_242765_a(world, generator, random, pos);
            }

            return false;
        }
    }
}
