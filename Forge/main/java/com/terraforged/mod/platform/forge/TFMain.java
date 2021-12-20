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

package com.terraforged.mod.platform.forge;

import com.terraforged.mod.Common;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.client.Client;
import com.terraforged.mod.command.DebugCommand;
import com.terraforged.mod.data.ModBiomes;
import com.terraforged.mod.platform.PlatformData;
import com.terraforged.mod.platform.forge.util.ForgeRegistrar;
import com.terraforged.mod.registry.registrar.NoopRegistrar;
import net.minecraft.core.IdMap;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

import java.nio.file.Path;

@Mod(TerraForged.MODID)
public class TFMain extends TerraForged {
    private final PlatformData platformData = new ForgePlatformData();

    public TFMain() {
        super(TFMain::getRootPath);
        MinecraftForge.EVENT_BUS.addListener(this::onServerStart);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onClientInit);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Biome.class, this::onBiomes);

        // Biomes are registered via event
        setRegistrar(Registry.BIOME_REGISTRY, NoopRegistrar.none());
    }

    @Override
    public PlatformData getData() {
        return platformData;
    }

    void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(Common.INSTANCE::init);
    }

    void onClientInit(FMLClientSetupEvent event) {
        event.enqueueWork(Client.INSTANCE::init);
    }

    void onServerStart(RegisterCommandsEvent event) {
        DebugCommand.register(event.getDispatcher());
    }

    void onBiomes(RegistryEvent.Register<Biome> event) {
        ModBiomes.register(new ForgeRegistrar<>(event.getRegistry()));
    }

    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }

    private static class ForgePlatformData implements PlatformData {
        @Override
        public IdMap<BlockState> getBlockStateRegistry() {
            return net.minecraftforge.registries.GameData.getBlockStateIDMap();
        }
    }
}
