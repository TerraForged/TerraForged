/*
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

package com.terraforged.mod.client;

import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.client.gui.screen.ConfigScreen;
import com.terraforged.mod.client.gui.screen.DemoScreen;
import com.terraforged.mod.featuremanager.data.FolderDataPackFinder;
import com.terraforged.mod.featuremanager.data.ModDataPackFinder;
import com.terraforged.mod.util.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.ForgeWorldTypeScreens;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.io.File;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientEvents {

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        Log.info("Client setup");

        event.enqueueWork(() -> {
            Log.info("Adding DataPackFinder");
            Minecraft minecraft = event.getMinecraftSupplier().get();
            File dir = new File(minecraft.gameDir, "config/terraforged/datapacks");
            FolderDataPackFinder dataPackFinder = new FolderDataPackFinder(dir);
            minecraft.getResourcePackList().addPackFinder(dataPackFinder);
            minecraft.getResourcePackList().addPackFinder(new ModDataPackFinder());
            minecraft.getResourcePackList().reloadPacksFromFinders();
        });

        ForgeWorldTypeScreens.registerFactory(LevelType.TERRAFORGED, DemoScreen::new);
    }

    private static BiomeGeneratorTypeScreens.IFactory getFactory() {
        if (Environment.isDev()) {
            return ConfigScreen::new;
        }
        return DemoScreen::new;
    }
}
