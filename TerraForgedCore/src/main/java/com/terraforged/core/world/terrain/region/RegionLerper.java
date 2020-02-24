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

package com.terraforged.core.world.terrain.region;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

public class RegionLerper implements Populator {

    private final Populator lower;
    private final Populator upper;

    public RegionLerper(Populator lower, Populator upper) {
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        float alpha = cell.regionEdge;
        if (alpha == 0) {
            lower.apply(cell, x, y);
            return;
        }

        if (alpha == 1) {
            upper.apply(cell, x, y);
            return;
        }

        lower.apply(cell, x, y);
        float lowerValue = cell.value;

        upper.apply(cell, x, y);
        float upperValue = cell.value;

        cell.value = NoiseUtil.lerp(lowerValue, upperValue, alpha);
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {
        if (cell.regionEdge == 0) {
            lower.tag(cell, x, y);
            return;
        }
        upper.tag(cell, x, y);
    }
}
