package com.terraforged.mod.worldgen.biome.climate;

import com.terraforged.mod.registry.Seedable;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.world.level.biome.Climate;

import java.util.List;

public class Sampler implements Climate.Sampler, Seedable<Sampler> {
    protected static final float EPSILON = 0.999F;
    protected static final int CELL_OFFSET = 2467891;
    protected static final int WARP_X_OFFSET = 89673452;
    protected static final int WARP_Z_OFFSET = 35276;

    protected final long seed;
    protected final Module noise;
    protected final Climate.TargetPoint[] points;

    public Sampler(long seed, List<Climate.TargetPoint> points) {
        this(seed, points.toArray(Climate.TargetPoint[]::new));
    }

    public Sampler(long seed, Climate.TargetPoint[] points) {
        var warp = Source.build(0, 400, 4).lacunarity(2.75).gain(0.7);
        this.seed = seed;
        this.points = points;
        this.noise = Source.cell((int) seed + CELL_OFFSET, 400).warp(
                warp.seed((int) seed + WARP_X_OFFSET).simplex2(),
                warp.seed((int) seed + WARP_Z_OFFSET).simplex2(),
                200
        );
    }

    @Override
    public Sampler withSeed(long seed) {
        return new Sampler(seed, points);
    }

    @Override
    public Climate.TargetPoint sample(int x, int y, int z) {
        float value = noise.getValue(x << 2, z << 2) * EPSILON;
        int index = NoiseUtil.floor(value * points.length);
        return points[index];
    }
}
