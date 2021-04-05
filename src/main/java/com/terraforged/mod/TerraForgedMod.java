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

package com.terraforged.mod;

import com.terraforged.engine.Engine;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.mod.api.material.WGTags;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.feature.TagConfigFixer;
import com.terraforged.mod.server.command.TerraCommand;
import com.terraforged.mod.util.DataUtils;
import com.terraforged.mod.util.Environment;
import net.minecraftforge.event.TagsUpdatedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModContainer;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.minecraftforge.fml.loading.moddiscovery.ModFileInfo;
import net.minecraftforge.forgespi.language.IModInfo;

import java.io.File;
import java.util.Objects;
import java.util.function.Supplier;

@Mod(TerraForgedMod.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class TerraForgedMod {

    public static final String MODID = "terraforged";
    public static final File CONFIG_DIR = new File("config", MODID).getAbsoluteFile();
    public static final File PRESETS_DIR = new File(CONFIG_DIR, "presets");
    public static final File DATAPACK_DIR = new File(CONFIG_DIR, "datapacks");
    private static final Supplier<String> VERSION = LazySupplier.of(() -> ModList.get().getModContainerById(MODID)
            .map(ModContainer::getModInfo)
            .map(IModInfo::getVersion)
            .map(Objects::toString)
            .orElse("unknown"));

    public TerraForgedMod() {
        Environment.log();
        ModFileInfo modInfo = ModList.get().getModFileById(MODID);
        Log.info("Signature:  {}", modInfo.getCodeSigningFingerprint().orElse("UNSIGNED"));
        Log.info("Trust Data: {}", modInfo.getTrustData().orElse("UNTRUSTED"));
        Engine.init();
        WGTags.init();
    }

    @SubscribeEvent
    public static void setup(FMLCommonSetupEvent event) {
        Log.info("Common setup");
        DataUtils.initDirs(PRESETS_DIR, DATAPACK_DIR);
        TerraCommand.init();
        ConfigManager.init();
        event.enqueueWork(() -> {
            RegistrationEvents.registerCodecs();
            RegistrationEvents.registerMissingBiomeTypes();
        });
    }

    @SubscribeEvent
    public static void complete(FMLLoadCompleteEvent event) {
        // log version because people do dumb stuff like renaming jars
        Log.info("Loaded TerraForged version {}", getVersion());
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class ForgeEvents {
        @SubscribeEvent
        public static void update(TagsUpdatedEvent event) {
            Log.info("Tags Reloaded");
            WGTags.printTags();
            TagConfigFixer.reset();
        }
    }

    public static String getVersion() {
        return VERSION.get();
    }
}
