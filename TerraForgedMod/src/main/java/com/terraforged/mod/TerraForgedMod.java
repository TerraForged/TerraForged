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
import com.terraforged.feature.FeatureManager;
import com.terraforged.mod.command.TerraCommand;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.feature.feature.DiskFeature;
import com.terraforged.mod.feature.tree.SaplingManager;
import com.terraforged.mod.util.DataPackFinder;
import com.terraforged.mod.util.Environment;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import java.io.File;

/**
 * Author <dags@dags.me>
 */
@Mod("terraforged")
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TerraForgedMod {

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Log.info("Common setup");
        WGTags.init();
        TerraWorld.init();
        SaplingManager.init();
        TerraCommand.init();

        // temp fix
        BiomeDictionary.addTypes(Biomes.BAMBOO_JUNGLE, BiomeDictionary.Type.OVERWORLD);
        BiomeDictionary.addTypes(Biomes.BAMBOO_JUNGLE_HILLS, BiomeDictionary.Type.OVERWORLD);
    }

    @SubscribeEvent
    public static void complete(FMLLoadCompleteEvent event) {
        if (Environment.isDev()) {
            DataGen.dumpData();
        }
    }

    @SubscribeEvent
    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        FeatureManager.registerTemplates(event);
        event.getRegistry().register(DiskFeature.INSTANCE);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void serverStart(FMLServerAboutToStartEvent event) {
            Log.info("Adding DataPackFinder");
            File dir = event.getServer().getFile("config/terraforged/datapacks");
            DataPackFinder dataPackFinder = new DataPackFinder(dir);
            event.getServer().getResourcePacks().addPackFinder(dataPackFinder);
        }
    }
}
