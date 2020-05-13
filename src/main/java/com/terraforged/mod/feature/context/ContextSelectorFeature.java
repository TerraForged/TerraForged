package com.terraforged.mod.feature.context;

import com.terraforged.core.concurrent.ObjectPool;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public class ContextSelectorFeature extends Feature<ContextSelectorConfig> {

    public static final ContextSelectorFeature INSTANCE = new ContextSelectorFeature();

    public ContextSelectorFeature() {
        super(ContextSelectorConfig::deserialize);
        setRegistryName("terraforged", "context_selector");
    }

    @Override
    public boolean place(IWorld world, ChunkGenerator<? extends GenerationSettings> generator, Random random, BlockPos pos, ContextSelectorConfig config) {
        try (ObjectPool.Item<ChanceContext> item = ChanceContext.pooled(world)) {
            if (item == null) {
                return false;
            }

            ChanceContext context = item.getValue();
            context.setPos(pos);
            context.init(config.features.size());
            for (int i = 0; i < config.features.size(); i++) {
                ContextualFeature feature = config.features.get(i);
                float chance = feature.getChance(pos, context);
                context.record(i, chance);
            }

            int index = context.nextIndex(random);
            if (index > -1) {
                return config.features.get(index).feature.place(world, generator, random, pos);
            }

            return false;
        }
    }
}
