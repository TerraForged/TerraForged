package com.terraforged.mod.platform.forge;

import com.terraforged.mod.Common;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.client.Client;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(TerraForged.MODID)
public class TFMain extends TerraForged {
    public TFMain() {
        super(TFMain::getRootPath);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
    }

    void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(Common.INSTANCE::init);
    }

    void onClientInit(FMLClientSetupEvent event) {
        event.enqueueWork(Client.INSTANCE::init);
    }

    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }
}
