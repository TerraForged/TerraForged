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

import com.terraforged.core.filter.Erosion;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.filter.Smoothing;
import com.terraforged.core.filter.Steepness;
import com.terraforged.core.region.Region;
import com.terraforged.core.settings.FilterSettings;
import com.terraforged.core.world.terrain.Terrain;

public class WorldFilters {

    private final Erosion erosion;
    private final Smoothing smoothing;
    private final Steepness steepness;
    private final FilterSettings settings;

    public WorldFilters(GeneratorContext context) {
        context = context.copy();
        this.settings = context.settings.filters;
        this.erosion = new Erosion(context.settings, context.levels);
        this.smoothing = new Smoothing(context.settings, context.levels);
        this.steepness = new Steepness(1, 10F, context.terrain);
    }

    public void apply(Region region) {
        Filterable<Terrain> map = region.filterable();
        erosion.apply(map, region.getRegionX(), region.getRegionZ(), settings.erosion.iterations);
        smoothing.apply(map, region.getRegionX(), region.getRegionZ(), settings.smoothing.iterations);
        steepness.apply(map, region.getRegionX(), region.getRegionZ(), 1);
    }
}
