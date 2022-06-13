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

package com.terraforged.mod.worldgen.noise.climate;

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.Settings;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.climate.Moisture;
import com.terraforged.engine.world.climate.Temperature;
import com.terraforged.mod.util.MathUtil;
import com.terraforged.mod.worldgen.noise.continent.cell.CellShape;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;

public class ClimateNoise {
    private static final float MOISTURE_SIZE = 2.5F;

    private final int seed;
    private final float jitter = 0.8f;
    private final float frequency;
    private final CellShape cellShape = CellShape.SQUARE;

    private final Domain warp;
    private final Module moisture;
    private final Module temperature;

    private final ThreadLocal<ClimateSample> localSample = ThreadLocal.withInitial(ClimateSample::new);

    public ClimateNoise(GeneratorContext context) {
        this(context.seed, context.settings);
    }

    public ClimateNoise(Seed seed, Settings settings) {
        int biomeSize = settings.climate.biomeShape.biomeSize;
        float tempScaler = settings.climate.temperature.scale;
        float moistScaler = settings.climate.moisture.scale * MOISTURE_SIZE;

        float biomeFreq = 1F / biomeSize;
        float moistureSize = moistScaler * biomeSize;
        float temperatureSize = tempScaler * biomeSize;
        int moistScale = NoiseUtil.round(moistureSize * biomeFreq);
        int tempScale = NoiseUtil.round(temperatureSize * biomeFreq);
        int warpScale = settings.climate.biomeShape.biomeWarpScale;

        this.seed = seed.next();
        this.frequency = 1F / biomeSize;

        Seed moistureSeed = seed.offset(settings.climate.moisture.seedOffset);
        Module moisture = new Moisture(moistureSeed.next(), moistScale, settings.climate.moisture.falloff);
        this.moisture = settings.climate.moisture.apply(moisture)
                .warp(moistureSeed.next(), Math.max(1, moistScale / 2), 1, moistScale / 4D)
                .warp(moistureSeed.next(), Math.max(1, moistScale / 6), 2, moistScale / 12D);

        Seed tempSeed = seed.offset(settings.climate.temperature.seedOffset);
        Module temperature = new Temperature(1F / tempScale, settings.climate.temperature.falloff);
        this.temperature = settings.climate.temperature.apply(temperature)
                .warp(tempSeed.next(), tempScale * 4, 2, tempScale * 4)
                .warp(tempSeed.next(), tempScale, 1, tempScale);


        this.warp = Domain.warp(
                Source.build(seed.next(), warpScale, 3).lacunarity(2.4).gain(0.3).simplex2(),
                Source.build(seed.next(), warpScale, 3).lacunarity(2.4).gain(0.3).simplex2(),
                Source.constant(settings.climate.biomeShape.biomeWarpStrength * 0.75)
        );
    }

    public ClimateSample getSample(int seed, float x, float y) {
        var sample = localSample.get().reset();
        sample(seed, x, y, sample);
        return sample;
    }

    public void sample(int seed, float x, float y, ClimateSample sample) {
        float px = warp.getX(seed, x, y);
        float py = warp.getY(seed, x, y);

        px *= frequency;
        py *= frequency;

        px = cellShape.adjustX(px);
        py = cellShape.adjustY(py);

        sampleBiome(seed, px, py, sample);

        sample.climateType = BiomeType.get(sample.temperature, sample.moisture);

        // TODO: Remove
        if (sample.climateType == BiomeType.COLD_STEPPE || sample.climateType == BiomeType.STEPPE) {
            sample.climateType = BiomeType.GRASSLAND;
        }
    }

    private void sampleBiome(int seed, float x, float y, ClimateSample sample) {
        int minX = NoiseUtil.floor(x) - 1;
        int minY = NoiseUtil.floor(y) - 1;
        int maxX = NoiseUtil.floor(x) + 2;
        int maxY = NoiseUtil.floor(y) + 2;

        float nearestX = x;
        float nearestY = y;
        int nearestHash = 0;
        float distance = Float.MAX_VALUE;
        float distance2 = Float.MAX_VALUE;

        for (int cy = minY; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++) {
                int hash = MathUtil.hash(seed, cx, cy);
                float px = cellShape.getCellX(hash, cx, cy, jitter);
                float py = cellShape.getCellY(hash, cx, cy, jitter);
                float d2 = NoiseUtil.dist2(x, y, px, py);

                if (d2 < distance) {
                    distance2 = distance;
                    distance = d2;
                    nearestX = px;
                    nearestY = py;
                    nearestHash = hash;
                } else if (d2 < distance2) {
                    distance2 = d2;
                }
            }
        }

        sample.biomeNoise = MathUtil.rand(nearestHash, 1236785);
        sample.biomeEdgeNoise = 1f - NoiseUtil.sqrt(distance / distance2);
        sample.moisture = this.moisture.getValue(seed, nearestX, nearestY);
        sample.temperature = this.temperature.getValue(seed, nearestX, nearestY);
    }
}
