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

import com.google.common.base.Suppliers;
import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.command.TFCommands;
import com.terraforged.mod.data.gen.TerraForgedDataProvider;
import com.terraforged.mod.lifecycle.CommonSetup;
import com.terraforged.mod.lifecycle.DataSetup;
import com.terraforged.mod.lifecycle.RegistrySetup;
import com.terraforged.mod.platform.forge.client.TFClient;
import com.terraforged.mod.platform.forge.registry.ForgeRegistry;
import com.terraforged.mod.registry.RegistryManager;
import com.terraforged.mod.registry.builtin.ModBuiltinRegistry;
import com.terraforged.mod.worldgen.biome.util.matcher.BiomeMatcher;
import com.terraforged.mod.worldgen.biome.util.matcher.BiomeTagMatcher;
import net.minecraft.tags.BiomeTags;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;

import java.nio.file.Path;
import java.util.function.Supplier;

@Mod(TerraForged.MODID)
public class TFMain extends TerraForged implements CommonAPI {
    private final Supplier<Path> container = Suppliers.memoize(TFMain::getRootPath);
    private final RegistryManager registryManager = new RegistryManager(ForgeRegistry::new, ModBuiltinRegistry::new);

    public TFMain() {
        CommonAPI.HOLDER.set(this);
        RegistrySetup.INSTANCE.run();
        DataSetup.INSTANCE.run();

        MinecraftForge.EVENT_BUS.addListener(this::onRegisterCommands);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onInit);
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::onGenerateData);

        if (FMLLoader.getDist().isClient()) {
            new TFClient();
        }
    }

    @Override
    public Path getContainer() {
        return container.get();
    }

    @Override
    public RegistryManager getRegistryManager() {
        return registryManager;
    }

    @Override
    public BiomeMatcher getOverworldMatcher() {
        return new BiomeTagMatcher.Overworld(BiomeTags.IS_OVERWORLD);
    }

    void onInit(FMLCommonSetupEvent event) {
        event.enqueueWork(CommonSetup.INSTANCE::run);
    }

    void onRegisterCommands(RegisterCommandsEvent event) {
        TFCommands.register(event.getDispatcher());
    }

    void onGenerateData(GatherDataEvent event) {
        // Init everything necessary to data-gen
        RegistrySetup.INSTANCE.run();
        CommonSetup.INSTANCE.run();

        var path = event.getGenerator().getOutputFolder().resolve("resources/default");

        event.getGenerator().addProvider(true, new TerraForgedDataProvider(path));
    }

    private static Path getRootPath() {
        return ModList.get().getModContainerById(MODID).orElseThrow().getModInfo().getOwningFile().getFile().getFilePath();
    }
}
