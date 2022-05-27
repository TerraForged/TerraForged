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

import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.mod.worldgen.biome.util.BiomeMapManager;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;
import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

public class BiomeSampler extends IBiomeSampler.Sampler implements IBiomeSampler {
    protected final BiomeMapManager biomeMapManager;
    protected final float beachSize = 0.005f;

    public BiomeSampler(INoiseGenerator noiseGenerator, BiomeMapManager biomeMapManager) {
        super(noiseGenerator);
        this.biomeMapManager = biomeMapManager;
    }

    public Holder<Biome> sampleBiome(int x, int z) {
        var sample = getSample(x, z);
        var biome = getInitialBiome(sample.biomeNoise, sample.climateType);
        return getBiomeOverride(biome, sample);
    }

    private Holder<Biome> getInitialBiome(float noise, BiomeType climateType) {
        var map = biomeMapManager.getBiomeMap().get(climateType);
        if (map == null || map.isEmpty()) {
            return biomeMapManager.getBiomes().getHolderOrThrow(Biomes.PLAINS);
        }
        return map.getValue(noise);
    }

    protected Holder<Biome> getBiomeOverride(Holder<Biome> input, ClimateSample sample) {
        var biomeType = sample.climateType;

        if (sample.continentNoise <= ContinentPoints.SHALLOW_OCEAN) {
            return switch (biomeType) {
                case TAIGA, COLD_STEPPE -> biomeMapManager.get(Biomes.DEEP_COLD_OCEAN);
                case TUNDRA -> biomeMapManager.get(Biomes.DEEP_FROZEN_OCEAN);
                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> biomeMapManager.get(Biomes.DEEP_LUKEWARM_OCEAN);
                default -> biomeMapManager.get(Biomes.DEEP_OCEAN);
            };
        }

        if (sample.continentNoise <= ContinentPoints.BEACH) {
            return switch (biomeType) {
                case TAIGA, COLD_STEPPE -> biomeMapManager.get(Biomes.COLD_OCEAN);
                case TUNDRA -> biomeMapManager.get(Biomes.FROZEN_OCEAN);
                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> biomeMapManager.get(Biomes.WARM_OCEAN);
                default -> biomeMapManager.get(Biomes.OCEAN);
            };
        }

        if (sample.continentNoise <= ContinentPoints.BEACH + beachSize) {
            return switch (biomeType) {
                case TUNDRA -> biomeMapManager.get(Biomes.SNOWY_BEACH);
                case COLD_STEPPE -> biomeMapManager.get(Biomes.STONY_SHORE);
                default -> biomeMapManager.get(Biomes.BEACH);
            };
        }

        if (sample.terrainType.isRiver() || sample.terrainType.isLake()) {
            return biomeType == BiomeType.TUNDRA ? biomeMapManager.get(Biomes.FROZEN_RIVER) : biomeMapManager.get(Biomes.RIVER);
        }

        return input;
    }
}
