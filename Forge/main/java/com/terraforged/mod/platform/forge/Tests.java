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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.test.VolcanoConfig;
import com.terraforged.mod.worldgen.test.VolcanoFeature;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Objects;

public class Tests {
    public interface Assets {
        ResourceKey<Feature<?>> VOLCANO_F = ResourceKey.create(Registry.FEATURE_REGISTRY, TerraForged.location("volcano"));
        ResourceKey<PlacedFeature> VOLCANO_PF = ResourceKey.create(Registry.PLACED_FEATURE_REGISTRY, TerraForged.location("volcano"));
        ResourceKey<ConfiguredFeature<?, ?>> VOLCANO_CF = ResourceKey.create(Registry.CONFIGURED_FEATURE_REGISTRY, TerraForged.location("volcano"));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class ModEvents {
        @SubscribeEvent
        public static void registerFeatures(RegistryEvent.Register<Feature<?>> features) {
            features.getRegistry().register(new VolcanoFeature().setRegistryName(Assets.VOLCANO_F.location()));
        }

        @SubscribeEvent
        public static void setup(FMLCommonSetupEvent event) {
            event.enqueueWork(() -> {
                System.out.println("Registering builtin:");

                var feature = (VolcanoFeature) ForgeRegistries.FEATURES.getValue(Assets.VOLCANO_F.location());
                Objects.requireNonNull(feature);

                var cfHolder = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, Assets.VOLCANO_CF, new ConfiguredFeature<>(feature, VolcanoConfig.defaultConfig()));

                BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, Assets.VOLCANO_PF, new PlacedFeature(cfHolder, List.of()));
            });
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void modify(BiomeLoadingEvent event) {
            System.out.println("Modifying biome: " + event.getName());

            var holder = BuiltinRegistries.PLACED_FEATURE.getOrCreateHolder(Assets.VOLCANO_PF);

            event.getGeneration().getFeatures(GenerationStep.Decoration.RAW_GENERATION).add(holder);
        }
    }
}
