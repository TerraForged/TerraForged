/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

public class NoiseCave implements ContextSeedable<NoiseCave>  {
    public static final Codec<NoiseCave> CODEC = LazyCodec.record(instance -> instance.group(
            CaveType.CODEC.fieldOf("type").forGetter(c -> c.type),
            NoiseCodec.CODEC.fieldOf("elevation").forGetter(c -> c.elevation),
            NoiseCodec.CODEC.fieldOf("shape").forGetter(c -> c.shape),
            NoiseCodec.CODEC.fieldOf("floor").forGetter(c -> c.floor),
            Codec.INT.fieldOf("size").forGetter(c -> c.size),
            Codec.INT.optionalFieldOf("min_y", -32).forGetter(c -> c.minY),
            Codec.INT.fieldOf("max_y").forGetter(c -> c.maxY)
    ).apply(instance, NoiseCave::new));

    private final CaveType type;
    private final Module elevation;
    private final Module shape;
    private final Module floor;
    private final int size;
    private final int minY;
    private final int maxY;
    private final int rangeY;

    public NoiseCave(CaveType type, Module elevation, Module shape, Module floor, int size, int minY, int maxY) {
        this.type = type;
        this.elevation = elevation;
        this.shape = shape;
        this.floor = floor;
        this.size = size;
        this.minY = minY;
        this.maxY = maxY;
        this.rangeY = maxY - minY;
    }

    @Override
    public NoiseCave withSeed(long seed) {
        var elevation = withSeed(seed, this.elevation, Module.class);
        var shape = withSeed(seed, this.shape, Module.class);
        var floor = withSeed(seed, this.floor, Module.class);
        return new NoiseCave(type, elevation, shape, floor, size, minY, maxY);
    }

    public CaveType getType() {
        return type;
    }

    public int getHeight(int x, int z) {
        return getScaleValue(x, z, 1F, minY, rangeY, elevation);
    }

    public int getCavernSize(int x, int z, float modifier) {
        return getScaleValue(x, z, modifier, 0, size, shape);
    }

    public int getFloorDepth(int x, int z, int size) {
        return getScaleValue(x, z, 1F, 0, size, floor);
    }

    @Override
    public String toString() {
        return "NoiseCave{" +
                "type=" + type +
                ", elevation=" + elevation +
                ", shape=" + shape +
                ", floor=" + floor +
                ", size=" + size +
                ", minY=" + minY +
                ", maxY=" + maxY +
                ", rangeY=" + rangeY +
                '}';
    }

    private static int getScaleValue(int x, int z, float modifier, int min, int range, Module noise) {
        if (range <= 0) return 0;

        return min + NoiseUtil.floor(noise.getValue(x, z) * range * modifier);
    }

    // 30, -32, 100
    public static NoiseCave megaCave(int seed, float scale, int minY, int maxY) {
        seed += 781249;

        int elevationScale = NoiseUtil.floor(200 * scale);
        int networkScale = NoiseUtil.floor(250 * scale);
        int floorScale = NoiseUtil.floor(30 * scale);
        int size = NoiseUtil.floor(30 *  scale);

        var elevation = Source.simplex(++seed, elevationScale, 2).map(0.3, 0.7);
        var shape = Source.simplex(++seed, networkScale, 3)
                .bias(-0.5).abs().scale(2).invert()
                .clamp(0.75, 1.0).map(0, 1);

        var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.3).map(0, 1);

        return new NoiseCave(CaveType.UNIQUE, elevation, shape, floor, size, minY, maxY);
    }

    public static NoiseCave synapseCave(int seed, float scale, int minY, int maxY) {
        seed += 79234;

        int elevationScale = NoiseUtil.floor(350 * scale);
        int networkScale = NoiseUtil.floor(180 * scale);
        int networkWarpScale = NoiseUtil.floor(20 * scale);
        int networkWarpStrength = networkWarpScale / 2;
        int floorScale = NoiseUtil.floor(20 * scale);
        int size = NoiseUtil.floor(15 *  scale);

        var elevation = Source.simplex(++seed, elevationScale, 3).map(0.1, 0.9);
        var shape = Source.simplexRidge(++seed, networkScale, 3)
                .warp(++seed, networkWarpScale, 1, networkWarpStrength)
                .clamp(0.35, 0.75).map(0, 1);
        var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.15).map(0, 1);

        return new NoiseCave(CaveType.GLOBAL, elevation, shape, floor, size, minY, maxY);
    }
}
