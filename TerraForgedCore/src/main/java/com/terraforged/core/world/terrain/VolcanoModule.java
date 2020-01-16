package com.terraforged.core.world.terrain;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.EdgeFunc;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.heightmap.RegionConfig;

public class VolcanoModule implements Module {

    private final Module cone;
    private final Module height;
    private final Module lowlands;
    private final float inversionPoint;
    private final float blendLower;
    private final float blendUpper;
    private final float blendRange;
    private final float bias;

    public VolcanoModule(Seed seed, RegionConfig region, Levels levels) {
        float midpoint = 0.3F;
        float range = 0.3F;

        Module heightNoise = Source.perlin(seed.next(), 2, 1).map(0.45, 0.6);

        this.height = Source.cellNoise(region.seed, region.scale, heightNoise)
                .warp(region.warpX, region.warpZ, region.warpStrength);

        this.cone = Source.cellEdge(region.seed, region.scale, EdgeFunc.DISTANCE_2_DIV).invert()
                .powCurve(14)
                .clamp(0.475, 1)
                .map(0, 1)
                .grad(0, 0.5, 0.5)
                .warp(seed.next(), 15, 2, 10)
                .scale(height);

        this.lowlands = Source.ridge(seed.next(), 150, 3)
                .warp(seed.next(), 30, 1, 30)
                .scale(0.1);

        this.inversionPoint = 0.95F;
        this.blendLower = midpoint - (range / 2F);
        this.blendUpper = blendLower + range;
        this.blendRange = blendUpper - blendLower;
        this.bias = levels.ground;
    }

    @Override
    public float getValue(float x, float z) {
        float value = cone.getValue(x, z);
        float limit = height.getValue(x, z);
        float maxHeight = limit * inversionPoint;

        // as value passes the inversion point we start calculating the inner-cone of the volcano
        if (value > maxHeight) {
            // modifies the steepness of the volcano inner-cone (larger == steeper)
            float steepnessModifier = 1;

            // as alpha approaches 1.0, position is closer to center of volcano
            float delta = (value - maxHeight) * steepnessModifier;
            float range = (limit - maxHeight);
            float alpha = delta / range;

            // calculate height inside volcano
            if (alpha > 0.99) {
                value = -bias + (1F / 255F);
            } else {
                value = maxHeight - ((maxHeight / 10F) * alpha);
            }
        } else if (value < blendLower) {
            value += lowlands.getValue(x, z);
        } else if (value < blendUpper) {
            float alpha = 1 - ((value - blendLower) / blendRange);
            value += (lowlands.getValue(x, z) * alpha);
        }

        return value + bias;
    }
}
