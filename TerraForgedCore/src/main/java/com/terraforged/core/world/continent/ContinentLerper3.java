package com.terraforged.core.world.continent;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.climate.Climate;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.func.Interpolation;
import me.dags.noise.util.NoiseUtil;

public class ContinentLerper3 implements Populator {

    private final Climate climate;
    private final Populator lower;
    private final Populator middle;
    private final Populator upper;
    private final float midpoint;

    private final float blendLower;
    private final float blendUpper;

    private final float lowerRange;
    private final float upperRange;

    public ContinentLerper3(Climate climate, Populator lower, Populator middle, Populator upper, float min, float mid, float max) {
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
        float select = cell.continentEdge;
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
        float select = cell.continentEdge;
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
            if (cell.value > cell.tag.getMax(climate.getRand().getValue(x, y))) {
                upper.tag(cell, x, y);
            }
        } else {
            upper.tag(cell, x, y);
        }
    }
}
