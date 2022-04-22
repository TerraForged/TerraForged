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
import com.terraforged.mod.platform.forge.util.ForgeUtil;
import com.terraforged.mod.worldgen.GeneratorPreset;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import net.minecraft.ChatFormatting;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraftforge.common.ForgeConfig;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.world.ForgeWorldPreset;

import java.nio.file.Files;

public class TFPreset implements ForgeWorldPreset.IBasicChunkGeneratorFactory {
    @Override
    public ChunkGenerator createChunkGenerator(RegistryAccess registryAccess, long seed) {
        return GeneratorPreset.build(seed, TerrainLevels.DEFAULT.get(), registryAccess);
    }

    public static ForgeWorldPreset create() {
        return ForgeUtil.withName(new ForgeWorldPreset(new TFPreset()) {
            @Override
            public String getTranslationKey() {
                return GeneratorPreset.TRANSLATION_KEY;
            }

            @Override
            public Component getDisplayName() {
                return new TranslatableComponent(getTranslationKey()).withStyle(s -> s.withColor(ChatFormatting.GREEN));
            }
        }, TerraForged.MODID);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public static void makeDefault() {
        if (Files.exists(DataPackExporter.CONFIG_DIR)) return;

        var current = ForgeConfig.COMMON.defaultWorldType.get();
        if (current.isEmpty() || current.equals("default")) {
            var configValue = (ForgeConfigSpec.ConfigValue) ForgeConfig.COMMON.defaultWorldType;
            configValue.set(TerraForged.MODID);
        }
    }
}
