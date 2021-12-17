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
import com.terraforged.engine.world.climate.ClimateModule;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.RiverCache;

public interface IBiomeSampler {
    ClimateModule getClimate();

    ClimateSample sample(int x, int z);

    float getShape(int x, int z);

    class Sampler implements IBiomeSampler {
        protected final NoiseLevels levels;
        protected final ClimateModule climateModule;
        protected final INoiseGenerator noiseGenerator;
        protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

        public Sampler(INoiseGenerator noiseGenerator) {
            this.levels = noiseGenerator.getLevels();
            this.climateModule = createClimate(noiseGenerator);
            this.noiseGenerator = noiseGenerator;
        }

        public INoiseGenerator getNoiseGenerator() {
            return noiseGenerator;
        }

        @Override
        public ClimateModule getClimate() {
            return climateModule;
        }

        @Override
        public ClimateSample sample(int x, int z) {
            float px = x * levels.frequency;
            float pz = z * levels.frequency;

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

        @Override
        public float getShape(int x, int z) {
            float px = x * levels.frequency;
            float pz = z * levels.frequency;

            var sample = localSample.get().reset();
            var cell = sample.cell;

            climateModule.apply(cell, px, pz);

            return sample.cell.biomeRegionEdge;
        }
    }

    class ClimateSample extends NoiseSample {
        public final Cell cell = new Cell();
        public final RiverCache riverCache = new RiverCache();

        public ClimateSample reset() {
            cell.reset();
            heightNoise = 1;
            terrainType = TerrainType.FLATS;
            return this;
        }
    }

    static ClimateModule createClimate(INoiseGenerator generator) {
        if (generator == null) return null;
        return new ClimateModule(generator.getContinent(), generator.getContinent().getContext());
    }
}
