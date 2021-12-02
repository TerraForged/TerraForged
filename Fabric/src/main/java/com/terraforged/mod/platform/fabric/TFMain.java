package com.terraforged.mod.platform.fabric;

import com.terraforged.mod.Common;
import com.terraforged.mod.TerraForged;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

import java.nio.file.Path;

public class TFMain extends TerraForged implements ModInitializer {
    public TFMain() {
        super(TFMain::getRootPath);
    }

    @Override
    public void onInitialize() {
        Common.INSTANCE.init();
    }

    private static Path getRootPath() {
        return FabricLoader.getInstance().getModContainer(MODID).orElseThrow().getRootPath();
    }
}
