package com.terraforged.mod.platform.forge;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.init.ModInit;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(TerraForged.MODID)
public class TerraForgedMod extends TerraForged<ModContainer> {
    public TerraForgedMod() {
        super(ModList.get()::getModContainerById);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::init);
    }

    @Override
    public Path getContainer() {
        return getModContainer().getModInfo().getOwningFile().getFile().getFilePath();
    }

    @Override
    public Path getFile(Path path) {
        return getContainer().resolve(path);
    }

    void init(FMLCommonSetupEvent event) {
        event.enqueueWork(ModInit::init);
    }
}
