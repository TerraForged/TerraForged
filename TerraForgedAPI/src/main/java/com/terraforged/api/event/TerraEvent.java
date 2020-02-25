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

import com.terraforged.api.chunk.column.DecoratorManager;
import com.terraforged.api.chunk.surface.SurfaceManager;
import com.terraforged.api.material.geology.GeologyManager;
import com.terraforged.api.material.layer.LayerManager;
import com.terraforged.core.world.terrain.provider.TerrainProvider;
import com.terraforged.feature.modifier.FeatureModifiers;
import net.minecraftforge.eventbus.api.Event;

public abstract class TerraEvent<T> extends Event {

    private final T manager;

    public TerraEvent(T manager) {
        this.manager = manager;
    }

    public T getManager() {
        return manager;
    }

    /**
     * Can be used to register custom biome Surface decorators
     */
    public static class Surface extends TerraEvent<SurfaceManager> {

        public Surface(SurfaceManager manager) {
            super(manager);
        }
    }

    /**
     * Register additional layer blocks (such as Snow Layers)
     */
    public static class Layers extends TerraEvent<LayerManager> {

        public Layers(LayerManager manager) {
            super(manager);
        }
    }

    /**
     * Register custom Strata and Geologies
     */
    public static class Geology extends TerraEvent<GeologyManager> {

        public Geology(GeologyManager manager) {
            super(manager);
        }
    }

    /**
     * Register custom FeatureModifiers
     */
    public static class Features extends TerraEvent<FeatureModifiers> {

        public Features(FeatureModifiers manager) {
            super(manager);
        }
    }

    /**
     * Register custom Terrain Populators
     */
    public static class Terrain extends TerraEvent<TerrainProvider> {

        public Terrain(TerrainProvider provider) {
            super(provider);
        }
    }

    /**
     * Register custom ColumnDecorators
     *  - base decorators process chunk columns early in the generation process (before biome surfaces etc)
     *  - feature decorators process chunk columns late in the generation process (after all features have been placed)
     */
    public static class Decorators extends TerraEvent<DecoratorManager> {

        public Decorators(DecoratorManager manager) {
            super(manager);
        }
    }
}
