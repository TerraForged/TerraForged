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

package com.terraforged.mod.util.setup;

import com.terraforged.mod.api.biome.modifier.ModifierManager;
import com.terraforged.mod.api.biome.surface.SurfaceManager;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.chunk.column.DecoratorManager;
import com.terraforged.mod.api.event.SetupEvent;
import com.terraforged.mod.api.material.geology.GeologyManager;
import com.terraforged.mod.api.material.layer.LayerManager;
import com.terraforged.mod.featuremanager.modifier.FeatureModifiers;
import com.terraforged.mod.featuremanager.structure.FMStructureManager;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.terrain.provider.TerrainProvider;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;

public class SetupHooks {

    public static <T extends TerrainProvider> T setup(T provider, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Terrain(provider, context));
        return provider;
    }

    public static <T extends SurfaceManager> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Surface(manager, context));
        return manager;
    }

    public static <T extends ModifierManager> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.BiomeModifier(manager, context));
        return manager;
    }

    public static <T extends LayerManager> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Layers(manager, context));
        return manager;
    }

    public static <T extends GeologyManager> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Geology(manager, context));
        return manager;
    }

    public static <T extends FeatureModifiers> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Features(manager, context));
        return manager;
    }

    public static <T extends FMStructureManager> T setup(T manager, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Structures(manager, context));
        return manager;
    }

    public static void setup(List<ColumnDecorator> base, List<ColumnDecorator> feature, GeneratorContext context) {
        MinecraftForge.EVENT_BUS.post(new SetupEvent.Decorators(new DecoratorManager(base, feature), context));
    }
}
