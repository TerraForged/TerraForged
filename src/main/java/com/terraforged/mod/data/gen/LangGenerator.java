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

package com.terraforged.mod.data.gen;

import com.terraforged.core.util.NameUtil;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.config.preview.PreviewSettings;
import com.terraforged.mod.util.TranslationKey;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.data.LanguageProvider;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

import java.util.function.UnaryOperator;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LangGenerator {

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        GuiKeys.init();
        GameContext context = GameContext.dynamic();

        LanguageProvider langProvider = new LanguageProvider(event.getGenerator(), "terraforged", "en_us") {
            @Override
            protected void addTranslations() {
                worlds(this);
                biomes(this, context);
                translationKeys(this);
                settings(this);
            }
        };

        event.getGenerator().addProvider(langProvider);
    }

    private static void worlds(LanguageProvider provider) {
        provider.add("generator.terraforged", "TerraForged");
        provider.add("generator.terratest", "TerraTest");
    }

    private static void biomes(LanguageProvider provider, GameContext context) {
        for (Biome biome : context.biomes) {
            ResourceLocation name = context.biomes.getRegistryName(biome);
            if (name != null && name.getNamespace().equals(TerraForgedMod.MODID)) {
//                provider.add(name.toString(), NameUtil.toDisplayName(name.getPath()));
            }
        }
    }

    private static void translationKeys(LanguageProvider provider) {
        TranslationKey.each(key -> provider.add(key.getKey(), key.getDefaultValue()));
    }

    private static void settings(LanguageProvider provider) {
        visit(NBTHelper.serialize(new DataGenSettings()), provider);
    }

    private static void visit(CompoundNBT tag, LanguageProvider lang) {
        tag.keySet().forEach(name -> {
            if (name.startsWith("#")) {
                return;
            }

            INBT value = tag.get(name);
            if (value instanceof CompoundNBT) {
                visit((CompoundNBT) value, lang);
            }

            CompoundNBT meta = tag.getCompound("#" + name);
            if (meta.isEmpty()) {
                return;
            }

            add(meta, "key", "display", NameUtil::toDisplayNameKey, lang);
            add(meta, "key", "comment", NameUtil::toTooltipKey, lang);
        });
    }

    private static void add(CompoundNBT tag, String keyName, String valName, UnaryOperator<String> keyFunc, LanguageProvider lang) {
        String key = keyFunc.apply(tag.getString(keyName));
        if (key.isEmpty()) {
            return;
        }

        String value = tag.getString(valName);
        if (value.isEmpty()) {
            return;
        }

        lang.add(key, value);
    }

    public static class DataGenSettings extends TerraSettings {

        public PreviewSettings preview = new PreviewSettings();
    }
}
