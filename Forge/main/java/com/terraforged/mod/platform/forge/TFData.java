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

import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.data.gen.TerraForgedDataProvider;
import com.terraforged.mod.lifecycle.CommonSetup;
import com.terraforged.mod.lifecycle.DataGenSetup;
import com.terraforged.mod.lifecycle.Stage;
import net.minecraft.core.Registry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.forge.event.lifecycle.GatherDataEvent;
import net.minecraftforge.registries.DeferredRegister;

public class TFData extends Stage {
    public static final TFData STAGE = new TFData();

    @Override
    protected void doInit() {
        var eventBus = FMLJavaModLoadingContext.get().getModEventBus();
        eventBus.addListener(this::onGenerateData);

        var register = DeferredRegister.create(Registry.BIOME_REGISTRY, TerraForged.MODID);
        register.register(eventBus);

        DataGenSetup.STAGE.run();

        for (var entry : CommonAPI.get().getRegistryManager().getRegistry(TerraForged.BIOMES)) {
            register.register(entry.getKey().location().getPath(), entry::getValue);
        }
    }

    void onGenerateData(GatherDataEvent event) {
        CommonSetup.STAGE.run();

        var path = event.getGenerator().getOutputFolder().resolve("resources/default");

        event.getGenerator().addProvider(true, new TerraForgedDataProvider(path));
    }
}
