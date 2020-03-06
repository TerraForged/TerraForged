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

package com.terraforged.core.world.decorator;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;

public class Wetlands implements Decorator {

    private final Module module;
    private final float poolBase;
    private final float bankHeight;

    private final Terrain wetlands;

    public Wetlands(Seed seed, Terrains terrain, Levels levels) {
        this.wetlands = terrain.wetlands;
        this.poolBase = levels.water(-3);
        this.bankHeight = levels.water(2);
        this.module = Source.perlin(seed.next(), 12, 1).clamp(0.35, 0.65).map(0, 1);
    }

    @Override
    public boolean apply(Cell<Terrain> cell, float x, float y) {
        if (cell.value < poolBase) {
            return false;
        }

        float tempAlpha = getAlpha(cell.temperature, 0.3F, 0.7F);
        if (tempAlpha == 0) {
            return false;
        }

        float moistAlpha = getAlpha(cell.moisture, 0.7F, 1F);
        if (moistAlpha == 0) {
            return false;
        }

        float riverAlpha = getAlpha(1 - cell.riverMask, 0.85F, 0.95F);
        if (riverAlpha == 0) {
            return false;
        }

        float alpha = tempAlpha * moistAlpha * riverAlpha;
        float value1 = NoiseUtil.lerp(cell.value, bankHeight, alpha);
        cell.value = Math.min(cell.value, value1);

        float poolAlpha = getAlpha(alpha, 0.35F, 0.55F);
        float shape = module.getValue(x, y);
        float value2 = NoiseUtil.lerp(cell.value, poolBase, shape * poolAlpha);
        cell.value = Math.min(cell.value, value2);

        if (poolAlpha > 0.5) {
            cell.tag = wetlands;
        }

        return true;
    }

    private static float getAlpha(float value, float min, float max) {
        return getAlpha(value, min, max, false);
    }

    private static float getAlpha(float value, float min, float max, boolean inverse) {
        if (value < min) {
            return 0F;
        }
        if (value >= max) {
            return 1F;
        }
        float alpha = (value - min) / (max - min);
        if (inverse) {
            return 1F - alpha;
        }
        return alpha;
    }
}
