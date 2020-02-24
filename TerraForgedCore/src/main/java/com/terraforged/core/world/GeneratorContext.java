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

package com.terraforged.core.world;

import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.core.world.terrain.provider.StandardTerrainProvider;
import com.terraforged.core.world.terrain.provider.TerrainProviderFactory;

public class GeneratorContext {

    public final Seed seed;
    public final Levels levels;
    public final Terrains terrain;
    public final Settings settings;
    public final TerrainProviderFactory terrainFactory;

    public GeneratorContext(Terrains terrain, Settings settings) {
        this(terrain, settings, StandardTerrainProvider::new);
    }

    public GeneratorContext(Terrains terrain, Settings settings, TerrainProviderFactory terrainFactory) {
        this.terrain = terrain;
        this.settings = settings;
        this.seed = new Seed(settings.generator.seed);
        this.levels = new Levels(settings.generator);
        this.terrainFactory = terrainFactory;
    }

    private GeneratorContext(GeneratorContext src) {
        seed = new Seed(src.seed.get());
        levels = src.levels;
        terrain = src.terrain;
        settings = src.settings;
        terrainFactory = src.terrainFactory;
    }

    public GeneratorContext copy() {
        return new GeneratorContext(this);
    }
}
