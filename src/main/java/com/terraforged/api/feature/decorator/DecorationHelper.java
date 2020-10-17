package com.terraforged.api.feature.decorator;

import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

public interface DecorationHelper {

    ISeedReader getRegion();

    ChunkGenerator getGenerator();

    static DecorationContext getContext(WorldDecoratingHelper helper) {
        DecorationHelper decorationHelper = (DecorationHelper) helper;
        return DecorationContext.of(decorationHelper.getRegion(), decorationHelper.getGenerator());
    }
}
