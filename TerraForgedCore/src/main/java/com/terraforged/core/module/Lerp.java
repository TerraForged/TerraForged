package com.terraforged.core.module;

import me.dags.noise.Module;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.terrain.Terrain;

public class Lerp implements Populator {

    private final Module control;
    private final Populator lower;
    private final Populator upper;

    public Lerp(Module control, Populator lower, Populator upper) {
        this.control = control;
        this.lower = lower;
        this.upper = upper;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float y) {
        float alpha = control.getValue(x, y);
        cell.regionMask = alpha;

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
        float alpha = control.getValue(x, y);
        if (alpha == 0) {
            lower.tag(cell, x, y);
            return;
        }
        upper.tag(cell, x, y);
    }
}
