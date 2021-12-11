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

package com.terraforged.mod.worldgen.biome;

import com.terraforged.mod.data.ModBiomes;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.noise.util.Noise;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class CaveBiomeSampler {
    public static final int OFFSET = 124897;

    protected final int seed;
    protected final int scale;
    protected final float frequency;
    protected final Map<CaveType, WeightMap<Biome>> typeMap = new EnumMap<>(CaveType.class);

    public CaveBiomeSampler(long seed, int scale, Registry<Biome> registry, List<Biome> biomes) {
        this.seed = (int) seed + OFFSET;
        this.scale = scale;
        this.frequency = 1F / scale;

        var global = new Biome[]{registry.getOrThrow(ModBiomes.CAVE.key())};
        var special = biomes.stream().filter(b -> b.getBiomeCategory() == Biome.BiomeCategory.UNDERGROUND).toArray(Biome[]::new);
        this.typeMap.put(CaveType.GLOBAL, create(global));
        this.typeMap.put(CaveType.UNIQUE, create(special));
    }

    public CaveBiomeSampler(long seed, CaveBiomeSampler other) {
        this.seed = (int) seed + OFFSET;
        this.scale = other.scale;
        this.frequency = 1F / other.scale;
        this.typeMap.putAll(other.typeMap);
    }

    public Biome getUnderGroundBiome(int seed, int x, int z, CaveType type) {
        seed += this.seed;
        float noise = sample(seed, x, z, frequency);
        return typeMap.get(type).getValue(noise);
    }

    protected static float sample(int seed, int x, int z, float frequency) {
        float nx = x * frequency;
        float nz = z * frequency;
        float noise = (1 + Noise.singleSimplex(nx, nz, seed)) * 0.5F;
        return NoiseUtil.clamp(noise, 0F, 1F);
    }

    protected static WeightMap<Biome> create(Biome[] biomes) {
        var weights = new float[biomes.length];
        Arrays.fill(weights, 1);
        return new WeightMap<>(biomes, weights);
    }
}
