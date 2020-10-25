package com.terraforged.mod.server;

import com.terraforged.fm.data.FolderDataPackFinder;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.profiler.Profiler;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppedEvent;

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

    @SubscribeEvent
    public static void serverStop(FMLServerStoppedEvent event) {
        File dir = event.getServer().getFile("dumps");
        Profiler.dump(dir);
    }
}
