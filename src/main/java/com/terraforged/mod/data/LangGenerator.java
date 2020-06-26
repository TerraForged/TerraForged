package com.terraforged.mod.data;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.core.util.NameUtil;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.preview2.PreviewSettings;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.UnaryOperator;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class LangGenerator {

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        GuiKeys.init();

        LanguageProvider langProvider = new LanguageProvider(event.getGenerator(), "terraforged", "en_us") {
            @Override
            protected void addTranslations() {
                worlds(this);
                biomes(this);
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

    private static void biomes(LanguageProvider provider) {
        for (Biome biome : ForgeRegistries.BIOMES) {
            ResourceLocation name = biome.getRegistryName();
            if (name != null && name.getNamespace().equals("terraforged")) {
                provider.add(biome, NameUtil.toDisplayName(name.getPath()));
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
