/*
 *
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

package com.terraforged.api.event;

import com.terraforged.api.biome.modifier.ModifierManager;
import com.terraforged.api.chunk.column.DecoratorManager;
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.api.material.geology.GeologyManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.terrain.provider.TerrainProvider;
import com.terraforged.feature.modifier.FeatureModifiers;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

@FunctionalInterface
public interface SetupEvent<T> {
    /**
     * Can be used to register custom biome Surface decorators
     */
    Event<SetupEvent<SurfaceManager>> SURFACE = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<SurfaceManager> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register additional layer blocks (such as Snow Layers)
     */
    Event<SetupEvent<LayerManager>> LAYERS = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<LayerManager> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register custom Strata and Geologies
     */
    Event<SetupEvent<GeologyManager>> GEOLOGY = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<GeologyManager> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register custom BiomeModifiers
     */
    Event<SetupEvent<ModifierManager>> BIOME_MODIFIER = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<ModifierManager> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register custom FeatureModifiers
     */
    Event<SetupEvent<FeatureModifiers>> FEATURES = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<FeatureModifiers> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register custom Terrain Populators
     */
    Event<SetupEvent<TerrainProvider>> TERRAIN = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<TerrainProvider> handler : handlers) {
            handler.handle(manager, context);
        }
    });
    /**
     * Register custom ColumnDecorators
     *  - base decorators process chunk columns early in the generation process (before biome surfaces etc)
     *  - feature decorators process chunk columns late in the generation process (after all features have been placed)
     */
    Event<SetupEvent<DecoratorManager>> DECORATORS = EventFactory.createArrayBacked(SetupEvent.class, handlers -> (manager, context) -> {
        for (SetupEvent<DecoratorManager> handler : handlers) {
            handler.handle(manager, context);
        }
    });

    /**
     * Handle the setup event.
     * 
     * @param manager the Manager/Service being setup
     * @param context the generator setup context
     */
    void handle(T manager, GeneratorContext context);
}
