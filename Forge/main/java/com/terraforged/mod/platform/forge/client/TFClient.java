/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.platform.forge.client;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.client.Client;
import com.terraforged.mod.client.screen.ScreenUtil;
import com.terraforged.mod.platform.ClientAPI;
import com.terraforged.mod.util.DemoHandler;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class TFClient {
    public TFClient() {
        ClientAPI.HOLDER.set(new ForgeClientAPI());
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
        MinecraftForge.EVENT_BUS.addListener(this::onRenderOverlay);
    }

    void onClientInit(FMLClientSetupEvent event) {
        event.enqueueWork(Client.INSTANCE::init);
        event.enqueueWork(TFPreset::makeDefault);
    }

    void onRenderOverlay(RenderGameOverlayEvent event) {
        DemoHandler.renderOverlay(event.getMatrixStack());
    }

    private static class ForgeClientAPI implements ClientAPI {
        @Override
        public boolean hasPreset() {
            return true;
        }

        @Override
        public boolean isDefaultPreset() {
            return ForgeConfig.COMMON.defaultWorldType.get().equals(TerraForged.MODID);
        }

        @Override
        public boolean isPresetSelected(CreateWorldScreen screen) {
            return ScreenUtil.isPresetSelected(screen);
        }
    }
}
