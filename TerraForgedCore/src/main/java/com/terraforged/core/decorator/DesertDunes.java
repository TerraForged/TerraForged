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

package com.terraforged.core.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.CellFunc;

public class DesertDunes implements Decorator {

    private final Module module;
    private final float climateMin;
    private final float climateMax;
    private final float climateRange;

    private final Levels levels;
    private final Terrains terrains;
    private final Terrain dunes = new Terrain("dunes", 100);

    public DesertDunes(GeneratorContext context) {
        this.climateMin = 0.6F;
        this.climateMax = 0.85F;
        this.climateRange = climateMax - climateMin;
        this.levels = context.levels;
        this.terrains = context.terrain;
        this.module = Source.cell(context.seed.next(), 80, CellFunc.DISTANCE)
                .warp(context.seed.next(), 70, 1, 70)
                .scale(30 / 255D);
    }

    @Override
    public boolean apply(Cell<Terrain> cell, float x, float y) {
        float temp = cell.temperature;
        float moisture = 1 - cell.moisture;
        float climate = temp * moisture;
        if (climate < climateMin) {
            return false;
        }

        float duneHeight = module.getValue(x, y);
        float climateMask = climate > climateMax ? 1F : (climate - climateMin) / climateRange;
        float regionMask = cell.mask(0.4F, 0.5F, 0,0.8F);

        float height = duneHeight * climateMask * regionMask;
        cell.value += height;
        cell.tag = dunes;

        return height >= levels.unit;
    }
}
