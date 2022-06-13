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
import com.terraforged.mod.data.codec.LazyCodec;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

public class NoiseCave implements ContextSeedable<NoiseCave>  {
    public static final Codec<NoiseCave> CODEC = LazyCodec.record(instance -> instance.group(
            Codec.INT.optionalFieldOf("seed", 0).forGetter(c -> c.seed),
            CaveType.CODEC.fieldOf("type").forGetter(c -> c.type),
            NoiseCodec.CODEC.fieldOf("elevation").forGetter(c -> c.elevation),
            NoiseCodec.CODEC.fieldOf("shape").forGetter(c -> c.shape),
            NoiseCodec.CODEC.fieldOf("floor").forGetter(c -> c.floor),
            Codec.INT.fieldOf("size").forGetter(c -> c.size),
            Codec.INT.optionalFieldOf("min_y", -32).forGetter(c -> c.minY),
            Codec.INT.fieldOf("max_y").forGetter(c -> c.maxY)
    ).apply(instance, NoiseCave::new));

    private final int seed;
    private final CaveType type;
    private final Module elevation;
    private final Module shape;
    private final Module floor;
    private final int size;
    private final int minY;
    private final int maxY;
    private final int rangeY;

    public NoiseCave(int seed, CaveType type, Module elevation, Module shape, Module floor, int size, int minY, int maxY) {
        this.seed = seed;
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
        return new NoiseCave(this.seed, type, elevation, shape, floor, size, minY, maxY);
    }

    public int getSeed() {
        return seed;
    }

    public CaveType getType() {
        return type;
    }

    public int getHeight(int seed, int x, int z) {
        return getScaleValue(seed, x, z, 1F, minY, rangeY, elevation);
    }

    public int getCavernSize(int seed, int x, int z, float modifier) {
        return getScaleValue(seed, x, z, modifier, 0, size, shape);
    }

    public int getFloorDepth(int seed, int x, int z, int size) {
        return getScaleValue(seed, x, z, 1F, 0, size, floor);
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

    private static int getScaleValue(int seed, int x, int z, float modifier, int min, int range, Module noise) {
        if (range <= 0) return 0;

        return min + NoiseUtil.floor(noise.getValue(seed, x, z) * range * modifier);
    }
}
