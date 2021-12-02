package com.terraforged.mod.platform.fabric;

import com.terraforged.mod.TerraForged;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class TFFabric extends TerraForged implements ModInitializer {
    public TFFabric() {
        super(TFFabric::getRootPath);
    }

    @Override
    public void onInitialize() {
        init();
    }

    private static Path getRootPath() {
        return FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getRootPath();
    }
}
