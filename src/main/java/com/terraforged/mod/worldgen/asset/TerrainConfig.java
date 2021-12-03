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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.util.seed.ContextSeedable;
import com.terraforged.mod.worldgen.noise.NoiseCodec;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;

public record TerrainConfig(Terrain type, float weight, Module heightmap) implements ContextSeedable<TerrainConfig>, WeightMap.Weighted {
    public static final TerrainConfig NONE = new TerrainConfig(TerrainType.NONE, 0, Source.ZERO);

    public static final Codec<TerrainConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("terrain").xmap(TerrainType::get, Terrain::getName).forGetter(TerrainConfig::type),
            Codec.FLOAT.optionalFieldOf("weight", 1F).forGetter(TerrainConfig::weight),
            NoiseCodec.CODEC.fieldOf("noise").forGetter(TerrainConfig::heightmap)
    ).apply(instance, TerrainConfig::new));

    @Override
    public TerrainConfig withSeed(long seed) {
        var heightmap = withSeed(seed, heightmap(), Module.class);
        return new TerrainConfig(type, weight, heightmap);
    }
}
