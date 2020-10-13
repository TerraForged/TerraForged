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

package com.terraforged.api.level.client;

import com.terraforged.api.level.type.LevelType;
import com.terraforged.api.registry.Registries;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientLevelManager {

    private static final AtomicReference<ResourceLocation> DEFAULT = new AtomicReference<>();
    private static final Map<ResourceLocation, LevelOption> options = new ConcurrentHashMap<>();
    private static final Map<ResourceLocation, BiomeGeneratorTypeScreens.IFactory> screens = new ConcurrentHashMap<>();

    public static void register(ResourceLocation name, BiomeGeneratorTypeScreens.IFactory factory) {
        screens.put(name, factory);
    }

    public static void setDefault(ResourceLocation levelType) {
        DEFAULT.set(levelType);
    }

    public static Optional<BiomeGeneratorTypeScreens> getDefault() {
        ResourceLocation levelType = DEFAULT.get();
        if (levelType == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(options.get(levelType));
    }

    @SubscribeEvent
    public static void setup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            for (LevelType levelType : Registries.LEVEL_TYPES) {
                BiomeGeneratorTypeScreens.IFactory editScreen = screens.get(levelType.getRegistryName());
                LevelOption option = new LevelOption(levelType, editScreen);
                LevelOption.register(option);
                options.put(levelType.getRegistryName(), option);
            }
        });
    }
}
