package com.terraforged.api.client;

import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public interface WorldOptionsCallback {

    void update(DimensionGeneratorSettings settings);

    static void update(WorldOptionsScreen screen, DimensionGeneratorSettings settings) {
        if (screen instanceof WorldOptionsCallback) {
            ((WorldOptionsCallback) screen).update(settings);
        } else {
            WorldOptionsFallback.update(screen, settings);
        }
    }
}
