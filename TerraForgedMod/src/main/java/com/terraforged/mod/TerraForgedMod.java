/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod;

import com.terraforged.api.material.WGTags;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.feature.FeatureManager;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.feature.tree.SaplingManager;
import com.terraforged.mod.settings.SettingsHelper;
import com.terraforged.mod.util.Environment;
import net.minecraft.server.dedicated.ServerProperties;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLDedicatedServerSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerStoppingEvent;

/**
 * Author <dags@dags.me>
 */
@Mod("terraforged")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TerraForgedMod {

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Log.info("Common setup");
        MinecraftForge.EVENT_BUS.addListener(TerraForgedMod::onShutdown);
        WGTags.init();
        TerraWorld.init();
        SaplingManager.init();
    }

    @SubscribeEvent
    public static void complete(FMLLoadCompleteEvent event) {
        if (Environment.isDev()) {
            DataGen.dumpData();
        }
    }

    //
    @SubscribeEvent
    //FoundSpore: Possibly useless event with the other method `serverSetup` replacing this.
    public static void server(FMLDedicatedServerSetupEvent event) {
        Log.info("Setting dedicated server");
        SettingsHelper.setDedicatedServer();
    }

    @SubscribeEvent
    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        FeatureManager.registerTemplates(event);
    }

    private static void onShutdown(FMLServerStoppingEvent event) {
        ThreadPool.shutdownCurrent();
    }
    @SubscribeEvent
    public static void serverSetup(FMLDedicatedServerSetupEvent event)
    {
        ServerProperties serverProperties = event.getServerSupplier().get().getServerProperties();
        if(serverProperties != null)
        {
            //get entry if it exists or null if it doesn't
            String entryValue = serverProperties.serverProperties.getProperty("use-modded-worldtype");

            if(entryValue != null && entryValue.equals("terraforged"))
            {
                //make server use our worldtype
                serverProperties.worldType = TerraWorld.TERRA;
                Log.info("TerraForged world type set for server!");
            }
        }
        //In server.properties use this line: `use-modded-worldtype=terraforged`
    }
}
