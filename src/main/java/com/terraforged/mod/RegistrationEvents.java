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

import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.feature.TerraFeatures;
import com.terraforged.mod.feature.context.ContextSelectorFeature;
import com.terraforged.mod.feature.decorator.FilterDecorator;
import com.terraforged.mod.feature.decorator.poisson.FastPoissonAtSurface;
import com.terraforged.mod.feature.feature.BushFeature;
import com.terraforged.mod.feature.feature.DiskFeature;
import com.terraforged.mod.feature.feature.FreezeLayer;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.placement.Placement;
import net.minecraftforge.common.BiomeManager;
import net.minecraftforge.common.world.ForgeWorldType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class RegistrationEvents {

    static void registerCodecs() {
        Registry.register(Registry.BIOME_PROVIDER_CODEC, TerraForgedMod.MODID + ":climate", TFBiomeProvider.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR_CODEC, TerraForgedMod.MODID + ":generator", TFChunkGenerator.CODEC);
    }

    static void registerMissingBiomeTypes() {
        BiomeManager.addBiome(BiomeManager.BiomeType.ICY, new BiomeManager.BiomeEntry(Biomes.ICE_SPIKES, 2));
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(Biomes.MUSHROOM_FIELDS, 2));
        BiomeManager.addBiome(BiomeManager.BiomeType.WARM, new BiomeManager.BiomeEntry(Biomes.MUSHROOM_FIELD_SHORE, 2));
    }

    @SubscribeEvent
    public static void registerLevels(RegistryEvent.Register<ForgeWorldType> event) {
        Log.info("Registering level types");
        event.getRegistry().register(LevelType.TERRAFORGED);
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
        event.getRegistry().register(FilterDecorator.INSTANCE);
        event.getRegistry().register(FastPoissonAtSurface.INSTANCE);
    }
}
