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
import com.terraforged.mod.command.TFCommands;
import com.terraforged.mod.data.ModBiomes;
import com.terraforged.mod.data.gen.DataGen;
import com.terraforged.mod.platform.CommonAPI;
import com.terraforged.mod.platform.forge.client.TFClient;
import com.terraforged.mod.platform.forge.client.TFPreset;
import com.terraforged.mod.platform.forge.util.ForgeRegistrar;
import com.terraforged.mod.registry.registrar.NoopRegistrar;
import com.terraforged.mod.util.DemoHandler;
import net.minecraft.core.Registry;
import net.minecraft.data.DataProvider;
import net.minecraft.data.HashCache;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.common.BiomeDictionary;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.world.ForgeWorldPreset;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Timer;
import java.util.TimerTask;

@Mod(TerraForged.MODID)
public class TFMain extends TerraForged {
    public TFMain() {
        super(TFMain::getRootPath);
        CommonAPI.HOLDER.set(new ForgeCommonAPI());

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        MinecraftForge.EVENT_BUS.addListener(this::onJoinWorld);

        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onGenerateData);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(Biome.class, this::onBiomes);
        FMLJavaModLoadingContext.get().getModEventBus().addGenericListener(ForgeWorldPreset.class, this::onPresets);

        // Biomes are registered via event
        setRegistrar(Registry.BIOME_REGISTRY, NoopRegistrar.none());

        if (FMLLoader.getDist().isClient()) {
            new TFClient();
        }
    }

    void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(Common.INSTANCE::init);
    }

    void onRegisterCommands(RegisterCommandsEvent event) {
        TFCommands.register(event.getDispatcher());
    }

    void onJoinWorld(PlayerEvent.PlayerLoggedInEvent event) {
        DemoHandler.warn(event.getPlayer());
    }

    void onBiomes(RegistryEvent.Register<Biome> event) {
        ModBiomes.register(new ForgeRegistrar<>(event.getRegistry()));
    }

    void onPresets(RegistryEvent.Register<ForgeWorldPreset> event) {
        TerraForged.LOG.debug("Registering world-preset");
        event.getRegistry().register(TFPreset.create());
    }

    void onGenerateData(GatherDataEvent event) {
        Common.INSTANCE.init();

        var path = event.getGenerator().getOutputFolder().resolve("resources/default");
        event.getGenerator().addProvider(new DataProvider() {
            @Override
            public String getName() {
                return "TerraForged";
            }

            @Override
            public void run(HashCache cache) throws IOException {
                try {
                    DataGen.export(path);
                } catch (Throwable t) {
                    t.printStackTrace();
                }

                new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        // Process hangs so force exit after completion
                        TerraForged.LOG.warn("Forcibly shutting down datagen process");
                        System.exit(0);
                    }
                }, 1_000L);
            }
        });

    }

    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }

    private static class ForgeCommonAPI implements CommonAPI {
        @Override
        public boolean isOverworldBiome(ResourceKey<Biome> key) {
            return BiomeDictionary.hasType(key, BiomeDictionary.Type.OVERWORLD);
        }
    }
}
