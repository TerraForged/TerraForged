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

package com.terraforged.mod;

import com.terraforged.api.level.type.LevelType;
import com.terraforged.api.material.WGTags;
import com.terraforged.api.registry.Registries;
import com.terraforged.fm.data.FolderDataPackFinder;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.feature.TerraFeatures;
import com.terraforged.mod.feature.context.ContextSelectorFeature;
import com.terraforged.mod.feature.decorator.poisson.PoissonAtSurface;
import com.terraforged.mod.feature.feature.BushFeature;
import com.terraforged.mod.feature.feature.DiskFeature;
import com.terraforged.mod.feature.feature.FreezeLayer;
import com.terraforged.mod.server.command.TerraCommand;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import java.io.File;

@Mod(TerraForgedMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TerraForgedMod {

    public static final String MODID = "terraforged";

    public TerraForgedMod() {
        Registries.init();
        Registry.register(Registry.BIOME_PROVIDER_CODEC, "terraforged:climate", TerraBiomeProvider.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, "terraforged:terrain", TerraChunkGenerator.CODEC);
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Log.info("Common setup");
        WGTags.init();
        TerraCommand.init();
        ConfigManager.init();
        SettingsHelper.init();
    }

    @SubscribeEvent
    public static void registerLevels(RegistryEvent.Register<LevelType> event) {
        event.getRegistry().register(new TerraForgedLevel().setRegistryName(TerraForgedLevel.NAME));
    }

    @SubscribeEvent
    public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
        Log.info("Registering features");
        event.getRegistry().register(TerraFeatures.INSTANCE);
        event.getRegistry().register(DiskFeature.INSTANCE);
        event.getRegistry().register(FreezeLayer.INSTANCE);
        event.getRegistry().register(BushFeature.INSTANCE);
        event.getRegistry().register(ContextSelectorFeature.INSTANCE);
    }

    @SubscribeEvent
    public static void registerDecorators(RegistryEvent.Register<Placement<?>> event) {
        Log.info("Registering decorators");
        event.getRegistry().register(PoissonAtSurface.INSTANCE);
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void serverStart(FMLServerAboutToStartEvent event) {
            Log.info("Adding DataPackFinder");
            File dir = event.getServer().getFile("config/terraforged/datapacks");
            FolderDataPackFinder dataPackFinder = new FolderDataPackFinder(dir);
            event.getServer().getResourcePacks().addPackFinder(dataPackFinder);
        }

        @SubscribeEvent
        public static void update(TagsUpdatedEvent event) {
            Log.info("Tags Reloaded");
            WGTags.printTags();
        }
    }
}
