package com.terraforged.core.module;

import me.dags.noise.Module;
import me.dags.noise.func.Interpolation;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.Terrain;

public class Blender extends Select implements Populator {

    private final Populator lower;
    private final Populator upper;

    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float midpoint;
    private final float tagThreshold;

    private boolean mask = false;

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

    public Blender mask() {
        mask = true;
        return this;
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

        if (mask) {
            cell.mask *= alpha;
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
