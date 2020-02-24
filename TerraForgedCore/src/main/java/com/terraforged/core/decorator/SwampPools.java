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
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

public class SwampPools implements Decorator {

    private final Module module;
    private final Levels levels;
    private final Terrains terrains;
    private final float minY;
    private final float maxY;
    private final float blendY;
    private final float blendRange;

    public SwampPools(Seed seed, Terrains terrains, Levels levels) {
        this.levels = levels;
        this.terrains = terrains;
        this.minY = levels.water(-3);
        this.maxY = levels.water(1);
        this.blendY = levels.water(4);
        this.blendRange = blendY - maxY;
        this.module = Source.perlin(seed.next(), 14, 1).clamp(0.45, 0.8).map(0, 1);
    }

    @Override
    public boolean apply(Cell<Terrain> cell, float x, float y) {
        if (cell.tag == terrains.ocean) {
            return false;
        }

        if (cell.moisture < 0.7 || cell.temperature < 0.3) {
            return false;
        }

        if (cell.value <= minY) {
            return false;
        }

        if (cell.value > blendY) {
            return false;
        }

        float alpha = module.getValue(x, y);
        if (cell.value > maxY) {
            float delta = blendY - cell.value;
            float alpha2 = delta / blendRange;
            alpha *= alpha2;
        }

        cell.value = NoiseUtil.lerp(cell.value, minY, alpha);
        return true;
    }
}
