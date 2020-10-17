package com.terraforged.mod.server;

import com.terraforged.fm.data.FolderDataPackFinder;
import com.terraforged.mod.Log;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import java.io.File;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class TerraServerEvents {

    @SubscribeEvent
    public static void serverStart(FMLServerAboutToStartEvent event) {
        Log.info("Adding DataPackFinder");
        File dir = event.getServer().getFile("config/terraforged/datapacks");
        FolderDataPackFinder dataPackFinder = new FolderDataPackFinder(dir);
        event.getServer().getResourcePacks().addPackFinder(dataPackFinder);
    }
}
