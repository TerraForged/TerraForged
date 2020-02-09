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
