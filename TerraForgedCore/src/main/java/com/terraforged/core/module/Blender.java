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
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.func.Interpolation;
import me.dags.noise.util.NoiseUtil;

public class Blender extends Select implements Populator {

    private final Populator lower;
    private final Populator upper;

    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float midpoint;
    private final float tagThreshold;

    public Blender(Module control, Populator lower, Populator upper, float min, float max, float split) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = blendUpper - blendLower;
        this.midpoint = blendLower + (blendRange * split);
        this.tagThreshold = midpoint;
    }

    public Blender(Populator control, Populator lower, Populator upper, float min, float max, float split, float tagThreshold) {
        super(control);
        this.lower = lower;
        this.upper = upper;
        this.blendLower = min;
        this.blendUpper = max;
        this.blendRange = blendUpper - blendLower;
        this.midpoint = blendLower + (blendRange * split);
        this.tagThreshold = tagThreshold;
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

        float alpha = Interpolation.LINEAR.apply((select - blendLower) / blendRange);
        lower.apply(cell, x, y);

        float lowerVal = cell.value;
        Terrain lowerType = cell.tag;

        upper.apply(cell, x, y);
        float upperVal = cell.value;

        cell.value = NoiseUtil.lerp(lowerVal, upperVal, alpha);
        if (select < midpoint) {
            cell.tag = lowerType;
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

        if (select < tagThreshold) {
            lower.tag(cell, x, y);
        } else {
            upper.tag(cell, x, y);
        }
    }
}
