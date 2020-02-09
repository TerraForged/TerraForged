package com.terraforged.core.world.terrain;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.EdgeFunc;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.heightmap.RegionConfig;

public class VolcanoPopulator extends TerrainPopulator {

    private static final float throat_value = 0.925F;

    private final Module cone;
    private final Module height;
    private final Module lowlands;
    private final float inversionPoint;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float bias;

    private final Terrain inner;
    private final Terrain outer;

    public VolcanoPopulator(Seed seed, RegionConfig region, Levels levels, Terrains terrains) {
        super(Source.ZERO, terrains.volcano);
        float midpoint = 0.3F;
        float range = 0.3F;

        Module heightNoise = Source.perlin(seed.next(), 2, 1).map(0.45, 0.65);

        this.height = Source.cellNoise(region.seed, region.scale, heightNoise)
                .warp(region.warpX, region.warpZ, region.warpStrength);

        this.cone = Source.cellEdge(region.seed, region.scale, EdgeFunc.DISTANCE_2_DIV).invert()
                .warp(region.warpX, region.warpZ, region.warpStrength)
                .powCurve(11)
                .clamp(0.475, 1)
                .map(0, 1)
                .grad(0, 0.5, 0.5)
                .warp(seed.next(), 15, 2, 10)
                .scale(height);

        this.lowlands = Source.ridge(seed.next(), 150, 3)
                .warp(seed.next(), 30, 1, 30)
                .scale(0.1);

        this.inversionPoint = 0.94F;
        this.blendLower = midpoint - (range / 2F);
        this.blendUpper = blendLower + range;
        this.blendRange = blendUpper - blendLower;
        this.outer = terrains.volcano;
        this.inner = terrains.volcanoPipe;
        this.bias = levels.ground;
    }

    @Override
    public void apply(Cell<Terrain> cell, float x, float z) {
        float value = cone.getValue(x, z);
        float limit = height.getValue(x, z);
        float maxHeight = limit * inversionPoint;

        // as value passes the inversion point we start calculating the inner-cone of the volcano
        if (value > maxHeight) {
            // modifies the steepness of the volcano inner-cone (larger == steeper)
            float steepnessModifier = 1F;

            // as alpha approaches 1.0, position is closer to center of volcano
            float delta = (value - maxHeight) * steepnessModifier;
            float range = (limit - maxHeight);
            float alpha = delta / range;

            // calculate height inside volcano
            if (alpha > throat_value) {
                cell.tag = inner;
            }

            value = maxHeight - ((maxHeight / 5F) * alpha);
        } else if (value < blendLower) {
            value += lowlands.getValue(x, z);
            cell.tag = outer;
        } else if (value < blendUpper) {
            float alpha = 1 - ((value - blendLower) / blendRange);
            value += (lowlands.getValue(x, z) * alpha);
            cell.tag = outer;
        }

        cell.value = bias + value;
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float z) {
        float value = cone.getValue(x, z);
        float limit = height.getValue(x, z);
        float maxHeight = limit * inversionPoint;
        if (value > maxHeight) {
            float steepnessModifier = 1;

            // as alpha approaches 1.0, position is closer to center of volcano
            float delta = (value - maxHeight) * steepnessModifier;
            float range = (limit - maxHeight);
            float alpha = delta / range;

            // calculate height inside volcano
            if (alpha > throat_value) {
                cell.tag = inner;
            } else {
                cell.tag = outer;
            }
        } else {
            cell.tag = outer;
        }
    }
}
