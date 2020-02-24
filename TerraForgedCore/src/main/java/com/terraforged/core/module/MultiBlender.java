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

package com.terraforged.core.module;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.func.Interpolation;
import me.dags.noise.util.NoiseUtil;

public class MultiBlender extends Select implements Populator {

    private final Climate climate;
    private final Populator lower;
    private final Populator middle;
    private final Populator upper;
    private final float midpoint;

    private final float blendLower;
    private final float blendUpper;

    private final float lowerRange;
    private final float upperRange;

    public MultiBlender(Climate climate, Populator control, Populator lower, Populator middle, Populator upper, float min, float mid, float max) {
        super(control);
        this.climate = climate;
        this.lower = lower;
        this.upper = upper;
        this.middle = middle;

        this.midpoint = mid;
        this.blendLower = min;
        this.blendUpper = max;

        this.lowerRange = midpoint - blendLower;
        this.upperRange = blendUpper - midpoint;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        float select = getSelect(cell, x, y);
        if (select < blendLower) {
            lower.apply(cell, x, y);
            return;
        }

        if (select > blendUpper) {
            upper.apply(cell, x, y);
            return;
        }

        if (select < midpoint) {
            float alpha = Interpolation.CURVE3.apply((select - blendLower) / lowerRange);

            lower.apply(cell, x, y);
            float lowerVal = cell.value;
            Terrain lowerType = cell.tag;

            middle.apply(cell, x, y);
            float upperVal = cell.value;

            cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
        } else {
            float alpha = Interpolation.CURVE3.apply((select - midpoint) / upperRange);

            middle.apply(cell, x, y);
            float lowerVal = cell.value;

            upper.apply(cell, x, y);
            cell.value = NoiseUtil.lerp(lowerVal, cell.value, alpha);
        }
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {
        float select = getSelect(cell, x, y);
        if (select < blendLower) {
            lower.tag(cell, x, y);
            return;
        }

        if (select > blendUpper) {
            upper.tag(cell, x, y);
            return;
        }

        if (select < midpoint) {
            lower.tag(cell, x, y);
//            upper.tag(cell, x, y);
            if (cell.value > cell.tag.getMax(climate.getRand().getValue(x, y))) {
                upper.tag(cell, x, y);
            }
        } else {
            upper.tag(cell, x, y);
        }
    }
}
