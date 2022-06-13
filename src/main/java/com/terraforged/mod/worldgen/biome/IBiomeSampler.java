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

import com.terraforged.mod.worldgen.noise.INoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.mod.worldgen.noise.climate.ClimateNoise;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;

public interface IBiomeSampler {
    ClimateSample getSample(int seed, int x, int z);

    float getShape(int seed, int x, int z);

    void sample(int seed, int x, int z, ClimateSample sample);

    class Sampler implements IBiomeSampler {
        protected final NoiseLevels levels;
        protected final ClimateNoise climateNoise;
        protected final INoiseGenerator noiseGenerator;
        protected final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

        public Sampler(INoiseGenerator noiseGenerator) {
            this.levels = noiseGenerator.getLevels();
            this.climateNoise = createClimate(noiseGenerator);
            this.noiseGenerator = noiseGenerator;
        }

        public ClimateSample getSample() {
            return localSample.get().reset();
        }

        public INoiseGenerator getNoiseGenerator() {
            return noiseGenerator;
        }

        @Override
        public ClimateSample getSample(int seed, int x, int z) {
            float px = x * levels.frequency;
            float pz = z * levels.frequency;

            var sample = localSample.get().reset();
            noiseGenerator.getContinent().sampleContinent(seed, px, pz, sample);
            noiseGenerator.getContinent().sampleRiver(seed, px, pz, sample);
            climateNoise.sample(seed, px, pz, sample);

            return sample;
        }

        @Override
        public float getShape(int seed, int x, int z) {
            float px = x * levels.frequency;
            float pz = z * levels.frequency;

            var sample = localSample.get().reset();

            climateNoise.sample(seed, px, pz, sample);

            return sample.biomeEdgeNoise;
        }

        @Override
        public void sample(int seed, int x, int z, ClimateSample sample) {
            float px = x * levels.frequency;
            float pz = z * levels.frequency;
            climateNoise.sample(seed, px, pz, sample);
        }
    }

    static ClimateNoise createClimate(INoiseGenerator generator) {
        if (generator == null) return null;
        return new ClimateNoise(generator.getContinent().getContext());
    }
}
