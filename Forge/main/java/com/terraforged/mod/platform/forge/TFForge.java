package com.terraforged.mod.platform.forge;

import com.terraforged.mod.TerraForged;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(TerraForged.MODID)
public class TFForge extends TerraForged {
    public TFForge() {
        super(TFForge::getRootPath);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInit);
    }

    void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(super::init);
    }

    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }
}
