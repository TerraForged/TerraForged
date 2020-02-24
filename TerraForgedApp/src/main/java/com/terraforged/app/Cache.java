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

package com.terraforged.app;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.RegionGenerator;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;

public class Cache {

    private float offsetX = 0;
    private float offsetZ = 0;
    private float zoom = 0F;
    private boolean filter = true;
    private Terrains terrain;
    private Settings settings;
    private GeneratorContext context;
    private Region region;
    private RegionGenerator renderer;

    public Cache(int seed) {
        Settings settings = new Settings();
        settings.generator.seed = seed;
        this.settings = settings;
        this.terrain = Terrains.create(settings);
        this.context = new GeneratorContext(terrain, settings);
        this.renderer = RegionGenerator.builder()
                .factory(new WorldGeneratorFactory(context))
                .pool(ThreadPool.getCommon())
                .size(3, 2)
                .build();
    }

    public Settings getSettings() {
        return settings;
    }

    public Terrains getTerrain() {
        return terrain;
    }

    public Terrain getCenterTerrain() {
        Terrain tag = getCenterCell().tag;
        return tag == null ? terrain.ocean : tag;
    }

    public BiomeType getCenterBiomeType() {
        return getCenterCell().biomeType;
    }

    public int getCenterHeight() {
        return (int) (context.levels.worldHeight * getCenterCell().value);
    }

    public Cell<Terrain> getCenterCell() {
        int center = region.getBlockSize().size / 2;
        return region.getCell(center, center);
    }

    public Region getRegion() {
        return region;
    }

    public void update(float offsetX, float offsetZ, float zoom, boolean filters) {
        if (region == null) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.offsetX != offsetX || this.offsetZ != offsetZ) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.zoom != zoom) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.filter != filters) {
            record(offsetX, offsetZ, zoom, filters);
        }
    }

    private void record(float offsetX, float offsetZ, float zoom, boolean filters) {
        this.zoom = zoom;
        this.filter = filters;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        try {
            this.region = renderer.generateRegion(offsetX, offsetZ, zoom, filters);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
