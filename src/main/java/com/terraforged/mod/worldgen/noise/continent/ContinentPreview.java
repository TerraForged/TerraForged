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
import com.terraforged.mod.data.ModTerrains;
import com.terraforged.mod.util.ui.Previewer;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.noise.continent.config.ContinentConfig;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.NoiseUtil;

import java.awt.*;
import java.util.concurrent.ThreadLocalRandom;

public class ContinentPreview {
    public static int SEED = ThreadLocalRandom.current().nextInt();

    public static void main(String[] args) {
        Previewer.launch(() -> {
            var noise = create();
            return (x, y) -> {
                float value = noise.getValue(x, y);

                if (true) return Color.HSBtoRGB(0, 0, value);

                if (value > ContinentPoints.COAST) return Color.GREEN.getRGB();
                if (value > ContinentPoints.BEACH) return Color.YELLOW.getRGB();
                if (value > ContinentPoints.SHALLOW_OCEAN) return Color.BLUE.getRGB();
                if (value > ContinentPoints.DEEP_OCEAN) return Color.DARK_GRAY.getRGB();
                return Color.BLACK.getRGB();
            };
        });
    }

    private static Module create() {
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
        config.shape.thresholdFalloff = 0.6f;

        var terrainLevels = new TerrainLevels();

        var generator = new NoiseGenerator(SEED, terrainLevels, ModTerrains.Factory.getDefault(null));

        return new Noise(generator);
    }

    public record Noise(NoiseGenerator generator) implements Module {
        @Override
        public float getValue(float x, float y) {
            var sample = generator.getContinentNoiseSample((int) x, (int) y);

//            if (true) return sample.baseNoise;

            float a = sample.baseNoise * NoiseUtil.map(sample.riverNoise, 0.5f, 1.0f, 0.5f);
            float b = sample.continentNoise * NoiseUtil.map(sample.riverNoise, 0.5f, 1.0f, 0.5f);

            return NoiseUtil.lerp(a, b, 0.4f);
        }
    }
}
