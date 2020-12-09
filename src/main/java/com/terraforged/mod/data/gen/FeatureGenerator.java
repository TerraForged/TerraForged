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

import com.terraforged.fm.FeatureManager;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.data.FeatureInjectorProvider;
import com.terraforged.mod.chunk.SetupFactory;
import com.terraforged.mod.data.gen.feature.Ores;
import com.terraforged.mod.data.gen.feature.Sediments;
import com.terraforged.mod.data.gen.feature.Shrubs;
import com.terraforged.mod.data.gen.feature.Trees;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.GatherDataEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class FeatureGenerator {

    @SubscribeEvent
    public static void gather(GatherDataEvent event) {
        FeatureInjectorProvider provider = new FeatureInjectorProvider(event.getGenerator(), "terraforged");
        try (DataManager dataManager = SetupFactory.createDataManager()) {
            FeatureManager.initData(dataManager);
            Ores.addInjectors(provider);
            Sediments.addInjectors(provider);
            Shrubs.addInjectors(provider);
            Trees.addInjectors(provider);
        }

        event.getGenerator().addProvider(provider);
    }
}
