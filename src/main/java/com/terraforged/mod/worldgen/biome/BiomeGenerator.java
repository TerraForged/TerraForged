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

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.climate.ClimateModule;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.worldgen.biome.util.BiomeUtil;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.RiverCache;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BiomeGenerator {
    protected final Registry<Biome> biomes;
    protected final NoiseGenerator noiseGenerator;
    protected final ClimateModule climateModule;
    protected final Map<BiomeType, WeightMap<Biome>> biomeMap;
    protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

    public BiomeGenerator(NoiseGenerator noiseGenerator, Registry<Biome> registry, List<Biome> biomes) {
        this.biomes = registry;
        this.noiseGenerator = noiseGenerator;
        this.climateModule = createClimate(noiseGenerator);
        this.biomeMap = getBiomeMapping(registry, biomes);
    }

    public Biome generate(int x, int z) {
        var sample = sample(x, z);
        var map = biomeMap.get(sample.cell.biome);

        Biome biome;
        if (map == null || map.isEmpty()) {
            TerraForged.LOG.debug("Missing biome for type: {}", sample.cell.biome);
            biome = biomes.getOrThrow(Biomes.PLAINS);
        } else {
            biome = map.getValue(sample.cell.biomeRegionId);
        }

        return getBiome(biome, sample);
    }

    protected ClimateSample sample(int x, int z) {
        float px = x * noiseGenerator.getLevels().frequency;
        float pz = z * noiseGenerator.getLevels().frequency;

        var sample = localSample.get().reset();
        noiseGenerator.getContinent().sampleContinent(px, pz, sample);
        noiseGenerator.getContinent().sampleRiver(px, pz, sample, sample.riverCache);

        var cell = sample.cell;
        cell.value = sample.heightNoise;
        cell.terrain = sample.terrainType;
        cell.riverMask = sample.riverNoise;
        cell.continentEdge = sample.continentNoise;

        climateModule.apply(cell, px, pz);

        return sample;
    }

    protected Biome getBiome(Biome input, ClimateSample sample) {
        var biomeType = sample.cell.biome;
        var controls = noiseGenerator.getContinent().getControlPoints();

        if (sample.continentNoise < controls.shallowOcean) {
            if (sample.continentNoise < controls.beach) {
                return switch (biomeType) {
                    case TAIGA, COLD_STEPPE -> biomes.get(Biomes.DEEP_COLD_OCEAN);
                    case TUNDRA -> biomes.get(Biomes.DEEP_FROZEN_OCEAN);
                    case DESERT, SAVANNA, TROPICAL_RAINFOREST -> biomes.get(Biomes.DEEP_LUKEWARM_OCEAN);
                    default -> biomes.get(Biomes.DEEP_OCEAN);
                };
            }
        }

        if (sample.continentNoise < controls.beach) {
            return switch (biomeType) {
                case TAIGA, COLD_STEPPE -> biomes.get(Biomes.COLD_OCEAN);
                case TUNDRA -> biomes.get(Biomes.FROZEN_OCEAN);
                case DESERT, SAVANNA, TROPICAL_RAINFOREST -> biomes.get(Biomes.WARM_OCEAN);
                default -> biomes.get(Biomes.OCEAN);
            };
        }

        if (sample.cell.terrain.isRiver()) {
            return biomeType == BiomeType.TUNDRA ? biomes.get(Biomes.FROZEN_RIVER) : biomes.get(Biomes.RIVER);
        }

        return input;
    }

    protected static ClimateModule createClimate(NoiseGenerator generator) {
        if (generator == null) return null;
        return new ClimateModule(generator.getContinent(), generator.getContinent().getContext());
    }

    protected static Map<BiomeType, WeightMap<Biome>> getBiomeMapping(Registry<Biome> registry, List<Biome> biomes) {
        var types = getBiomeTypeMap(registry, biomes);
        var mapping = new EnumMap<BiomeType, WeightMap<Biome>>(BiomeType.class);

        for (var type : BiomeType.values()) {
            var list = types.get(type);
            if (list == null) continue;

            var map = getWeightedMap(list);
            mapping.put(type, map);
        }

        return mapping;
    }

    protected static WeightMap<Biome> getWeightedMap(List<Biome> biomes) {
        var biome = biomes.toArray(Biome[]::new);
        var weights = new float[biomes.size()];
        Arrays.fill(weights, 1.0F);
        return new WeightMap<>(biome, weights);
    }

    protected static Map<BiomeType, List<Biome>> getBiomeTypeMap(Registry<Biome> registry, List<Biome> biomes) {
        return biomes.stream()
                .sorted(BiomeUtil.getBiomeSorter(registry))
                .collect(Collectors.groupingBy(BiomeUtil::getType));
    }

    protected static class ClimateSample extends NoiseSample {
        public final Cell cell = new Cell();
        public final RiverCache riverCache = new RiverCache();

        public ClimateSample reset() {
            cell.reset();
            heightNoise = 1;
            terrainType = TerrainType.FLATS;
            return this;
        }
    }
}
