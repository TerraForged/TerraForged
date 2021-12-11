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
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

@SuppressWarnings("ClassCanBeRecord")
public class TerrainConfig implements ContextSeedable<TerrainConfig>, WeightMap.Weighted {
    public static final TerrainConfig NONE = new TerrainConfig(TerrainType.NONE, 0, Source.ZERO);

    public static final Codec<TerrainConfig> CODEC = LazyCodec.record(instance -> instance.group(
            Codec.STRING.fieldOf("terrain").xmap(TerrainType::get, Terrain::getName).forGetter(TerrainConfig::terrain),
            Codec.FLOAT.optionalFieldOf("weight", 1F).forGetter(TerrainConfig::weight),
            NoiseCodec.CODEC.fieldOf("noise").forGetter(TerrainConfig::noise)
    ).apply(instance, TerrainConfig::new));

    private final Terrain terrain;
    private final float weight;
    private final Module noise;

    public TerrainConfig(Terrain terrain, float weight, Module noise) {
        this.terrain = terrain;
        this.weight = weight;
        this.noise = noise;
    }

    @Override
    public TerrainConfig withSeed(long seed) {
        var heightmap = withSeed(seed, noise(), Module.class);
        return new TerrainConfig(terrain, weight, heightmap);
    }

    @Override
    public float weight() {
        return weight;
    }

    public Terrain terrain() {
        return terrain;
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
