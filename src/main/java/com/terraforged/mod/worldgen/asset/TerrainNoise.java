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
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.core.Holder;

import java.util.Comparator;

public class TerrainNoise implements ContextSeedable<TerrainNoise>, WeightMap.Weighted {
    public static final TerrainNoise NONE = new TerrainNoise(Holder.direct(TerrainType.NONE), 0, Source.ZERO);
    public static final Comparator<TerrainNoise> COMPARATOR = Comparator.comparing(t -> t.terrain().getName());

    public static final Codec<TerrainNoise> CODEC = LazyCodec.record(instance -> instance.group(
            TerrainType.CODEC.fieldOf("type").forGetter(TerrainNoise::type),
            Codec.FLOAT.fieldOf("weight").forGetter(TerrainNoise::weight),
            NoiseCodec.CODEC.fieldOf("noise").forGetter(TerrainNoise::noise)
    ).apply(instance, TerrainNoise::new));

    private static final double MIN_NOISE = 5.0 / 255.0;

    private final Holder<TerrainType> type;
    private final float weight;
    private final Module noise;

    public TerrainNoise(Holder<TerrainType> type, float weight, Module noise) {
        this.type = type;
        this.weight = weight;
        this.noise = noise.minValue() < MIN_NOISE ? noise.bias(MIN_NOISE).clamp(0, 1) : noise;
    }

    @Override
    public TerrainNoise withSeed(long seed) {
        var heightmap = withSeed(seed, noise(), Module.class);
        return new TerrainNoise(type, weight, heightmap);
    }

    @Override
    public float weight() {
        return weight;
    }

    public Holder<TerrainType> type() {
        return type;
    }

    public Terrain terrain() {
        return type().value().getTerrain();
    }

    public Module noise() {
        return noise;
    }

    @Override
    public String toString() {
        return "TerrainConfig{" +
                "type=" + type +
                ", weight=" + weight +
                ", noise=" + noise +
                '}';
    }

    static {
        NoiseCodec.init();
    }
}
