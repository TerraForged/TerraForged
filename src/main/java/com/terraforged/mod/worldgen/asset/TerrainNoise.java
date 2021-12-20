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

import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

import java.util.function.Supplier;

public class TerrainNoise implements ContextSeedable<TerrainNoise>, WeightMap.Weighted {
    public static final TerrainNoise NONE = new TerrainNoise(Suppliers.ofInstance(TerrainType.NONE), 0, Source.ZERO);

    public static final Codec<TerrainNoise> CODEC = LazyCodec.record(instance -> instance.group(
            TerrainType.CODEC.fieldOf("type").forGetter(TerrainNoise::type),
            Codec.FLOAT.fieldOf("weight").forGetter(TerrainNoise::weight),
            NoiseCodec.CODEC.fieldOf("noise").forGetter(TerrainNoise::noise)
    ).apply(instance, TerrainNoise::new));

    private final Supplier<TerrainType> type;
    private final float weight;
    private final Module noise;
    private final Supplier<Terrain> terrain;

    public TerrainNoise(TerrainType type, float weight, Module noise) {
        this(Suppliers.ofInstance(type), weight, noise);
    }

    public TerrainNoise(Supplier<TerrainType> type, float weight, Module noise) {
        this.type = type;
        this.weight = weight;
        this.noise = noise;
        this.terrain = Suppliers.memoize(() -> type.get().getTerrain());
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

    public Supplier<TerrainType> type() {
        return type;
    }

    public Terrain terrain() {
        return terrain.get();
    }

    public Module noise() {
        return noise;
    }

    @Override
    public String toString() {
        return "TerrainConfig{" +
                "terrain=" + terrain +
                ", weight=" + weight +
                ", noise=" + noise +
                '}';
    }
}
