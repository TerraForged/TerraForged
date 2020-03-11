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

import com.terraforged.api.material.MaterialTags;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.feature.FeatureManager;
import com.terraforged.mod.biome.tag.BiomeTagManager;
import com.terraforged.mod.command.TerraCommand;
import com.terraforged.mod.data.DataGen;
import com.terraforged.mod.feature.tree.SaplingManager;
import com.terraforged.mod.settings.SettingsHelper;
import com.terraforged.mod.util.Environment;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.server.ServerStartCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import net.fabricmc.fabric.api.registry.CommandRegistry;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.minecraft.resource.ResourceType;
import net.minecraft.server.MinecraftServer;
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
public class TerraForgedMod implements ModInitializer {

    @Override 
    public void onInitialize() {
        Log.info("Common setup");
        ServerStopCallback.EVENT.register(TerraForgedMod::onShutdown);
        MaterialTags.init();
        TerraWorld.init();
        SaplingManager.init();
        if (Environment.isDev()) {
            DataGen.dumpData();
        }
        FeatureManager.registerTemplates();
        ResourceManagerHelper.get(ResourceType.SERVER_DATA).registerReloadListener(new BiomeTagManager());
        CommandRegistry.INSTANCE.register(false, TerraCommand::register);
    }

    public static void server() {
        Log.info("Setting dedicated server");
        SettingsHelper.setDedicatedServer();
    }
    
    private static void onShutdown(MinecraftServer server) {
        ThreadPool.shutdownCurrent();
    }
}
