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

import com.terraforged.engine.Engine;
import com.terraforged.mod.api.material.WGTags;
import com.terraforged.mod.api.registry.Registries;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.data.WorldGenBiomes;
import com.terraforged.mod.featuremanager.GameContext;
import com.terraforged.mod.server.command.TerraCommand;
import com.terraforged.mod.util.version.VersionChecker;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

import java.io.File;

@Mod(TerraForgedMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TerraForgedMod {

    public static final String MODID = "terraforged";

    public TerraForgedMod() {
        VersionChecker.require("forge", 35, 1, 5);

        Engine.init();
        WGTags.init();
        Registries.init();

        Registry.register(Registry.BIOME_PROVIDER_CODEC, "terraforged:climate", TFBiomeProvider.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, "terraforged:generator", TFChunkGenerator.CODEC);

        BiomeManager.addBiome(BiomeManager.BiomeType.ICY, new BiomeManager.BiomeEntry(Biomes.ICE_SPIKES, 2));
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(Biomes.MUSHROOM_FIELDS, 2));
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(Biomes.MUSHROOM_FIELD_SHORE, 2));
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Log.info("Common setup");
        TerraCommand.init();
        ConfigManager.init();
//        SettingsHelper.init();

        event.enqueueWork(() -> {
            GameContext context = new GameContext(DynamicRegistries.func_239770_b_());
            File dataDir = new File("data").getAbsoluteFile();
            WorldGenBiomes.genBiomeMap(dataDir, context);
        });
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void update(TagsUpdatedEvent event) {
            Log.info("Tags Reloaded");
            WGTags.printTags();
        }
    }

    public static String getVersion() {
        return ModList.get().getModContainerById(MODID)
                .map(mod -> "TF-" + mod.getModInfo().getVersion().toString())
                .orElse("TF-????");
    }
}
