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

package com.terraforged.mod.worldgen.noise.continent;

import com.terraforged.engine.settings.WorldSettings;
import com.terraforged.mod.client.ui.Previewer;
import com.terraforged.mod.data.ModTerrains;
import com.terraforged.mod.util.ColorUtil;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.climate.ClimateNoise;
import com.terraforged.mod.worldgen.noise.climate.ClimateSample;
import com.terraforged.mod.worldgen.noise.continent.config.ContinentConfig;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.util.NoiseUtil;

import java.util.concurrent.ThreadLocalRandom;

public class ContinentPreview {
    public static int SEED = ThreadLocalRandom.current().nextInt();

    public static void main(String[] args) {
        Previewer.launch(() -> {
            var noise = create();
            return (x, y) -> {
                var sample = noise.getSample(273651, x, y);

                return ColorUtil.getBiomeColor(sample, 0.2f);
            };
        });
    }

    private static Noise create() {
        var controls = new WorldSettings.ControlPoints();
        controls.deepOcean = 0.05f;
        controls.shallowOcean = 0.3f;
        controls.beach = 0.45f;
        controls.coast = 0.75f;
        controls.inland = 0.80f;

        var config = new ContinentConfig();
        config.shape.seed0 = SEED;
        config.shape.seed1 = SEED + 39674;
        config.shape.threshold = 0.525f;

        var terrainLevels = new TerrainLevels();

        // -385290196

        var generator = new NoiseGenerator(terrainLevels, ModTerrains.Factory.getDefault(null));

        return new Noise(generator, new ClimateNoise(generator.getContinent().getContext()));
    }

    public record Noise(NoiseGenerator generator, ClimateNoise climate) {
        public ClimateSample getSample(int seed, float x, float y) {
            var sample = climate.getSample(seed, x, y);
            sampleContinent(seed, x, y, sample);
            sampleRivers(seed, x, y, sample);
            return sample;
        }

        public void sampleContinent(int seed, float x, float y, NoiseSample sample) {
            generator.sampleContinentNoise(seed, NoiseUtil.floor(x), NoiseUtil.floor(y), sample);
        }

        public void sampleRivers(int seed, float x, float y, NoiseSample sample) {
            generator.sampleRiverNoise(seed, NoiseUtil.floor(x), NoiseUtil.floor(y), sample);
        }
    }
}
