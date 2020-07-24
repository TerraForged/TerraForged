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
import com.terraforged.api.biome.surface.SurfaceManager;
import com.terraforged.api.chunk.column.DecoratorManager;
import com.terraforged.api.material.geology.GeologyManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.fm.modifier.FeatureModifiers;
import com.terraforged.fm.structure.StructureManager;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.terrain.provider.TerrainProvider;
import net.minecraftforge.eventbus.api.Event;

public abstract class SetupEvent<T> extends Event {

    private final T manager;
    private final GeneratorContext context;

    public SetupEvent(T manager, GeneratorContext context) {
        this.manager = manager;
        this.context = context;
    }

    /**
     * Returns the Manager/Service being setup
     */
    public T getManager() {
        return manager;
    }

    /**
     * Returns the generator setup context
     */
    public GeneratorContext getContext() {
        return context;
    }

    /**
     * Can be used to register custom biome Surface decorators
     */
    public static class Surface extends SetupEvent<SurfaceManager> {

        public Surface(SurfaceManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register additional layer blocks (such as Snow Layers)
     */
    public static class Layers extends SetupEvent<LayerManager> {

        public Layers(LayerManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register custom Strata and Geologies
     */
    public static class Geology extends SetupEvent<GeologyManager> {

        public Geology(GeologyManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register custom BiomeModifiers
     */
    public static class BiomeModifier extends SetupEvent<ModifierManager> {

        public BiomeModifier(ModifierManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register custom FeatureModifiers
     */
    public static class Features extends SetupEvent<FeatureModifiers> {

        public Features(FeatureModifiers manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register custom FeatureModifiers
     */
    public static class Structures extends SetupEvent<StructureManager> {

        public Structures(StructureManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }

    /**
     * Register custom Terrain Populators
     */
    public static class Terrain extends SetupEvent<TerrainProvider> {

        public Terrain(TerrainProvider provider, GeneratorContext context) {
            super(provider, context);
        }
    }

    /**
     * Register custom ColumnDecorators
     *  - base decorators process chunk columns early in the generation process (before biome surfaces etc)
     *  - feature decorators process chunk columns late in the generation process (after all features have been placed)
     */
    public static class Decorators extends SetupEvent<DecoratorManager> {

        public Decorators(DecoratorManager manager, GeneratorContext context) {
            super(manager, context);
        }
    }
}
