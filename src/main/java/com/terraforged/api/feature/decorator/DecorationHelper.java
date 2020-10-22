package com.terraforged.api.feature.decorator;

import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public interface DecorationHelper {

    ISeedReader getRegion();

    ChunkGenerator getGenerator();

    static DecorationContext getContext(WorldDecoratingHelper helper) {
        if (helper instanceof DecorationHelper) {
            DecorationHelper decorationHelper = (DecorationHelper) helper;
            return DecorationContext.of(decorationHelper.getRegion(), decorationHelper.getGenerator());
        }
        return DecorationHelperFallback.getContext(helper);
    }
}
