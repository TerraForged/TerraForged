/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.core.world.climate;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.settings.GeneratorSettings;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.func.EdgeFunc;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

public class ClimateModule {

    private final int seed;

    private final float edgeClamp;
    private final float edgeScale;
    private final float biomeFreq;
    private final float warpStrength;

    private final Module warpX;
    private final Module warpZ;
    private final Module moisture;
    private final Module temperature;

    public ClimateModule(Seed seed, GeneratorSettings settings) {
        int biomeSize = settings.biome.biomeSize;

        // todo - better solution (reduces the amount that temp/moist grows with biome size)
        int tempScaler = biomeSize > 500 ? 8 : 10;
        int moistScaler = biomeSize > 500 ? 20 : 40;

        float biomeFreq = 1F / biomeSize;
        int moistureSize = moistScaler * biomeSize;
        int temperatureSize = tempScaler * tempScaler;
        int moistScale = NoiseUtil.round(moistureSize * biomeFreq);
        int tempScale = NoiseUtil.round(temperatureSize * biomeFreq);
        int warpScale = settings.biome.biomeWarpScale;

        this.seed = seed.next();
        this.edgeClamp = 1F;
        this.edgeScale = 1 / edgeClamp;
        this.biomeFreq = 1F / biomeSize;
        this.warpStrength = settings.biome.biomeWarpStrength;
        this.warpX = Source.perlin(seed.next(), warpScale, 2).bias(-0.5);
        this.warpZ = Source.perlin(seed.next(), warpScale, 2).bias(-0.5);

        this.moisture = Source.simplex(seed.next(), moistScale, 2)
                .clamp(0.15, 0.85).map(0, 1)
                .warp(seed.next(), moistScale / 2, 1, moistScale / 4D)
                .warp(seed.next(), moistScale / 6, 2, moistScale / 12D);

        Module temperature = Source.sin(tempScale, Source.constant(0.9)).clamp(0.05, 0.95).map(0, 1);
        this.temperature = new Compressor(temperature, 0.1F, 0.2F)
                .warp(seed.next(), tempScale * 4, 2, tempScale * 4)
                .warp(seed.next(), tempScale, 1, tempScale)
                .warp(seed.next(), tempScale / 8, 1, tempScale / 8D);
    }

    public void apply(Cell<Terrain> cell, float x, float y, boolean mask) {
        float ox = warpX.getValue(x, y) * warpStrength;
        float oz = warpZ.getValue(x, y) * warpStrength;

        x += ox;
        y += oz;

        x *= biomeFreq;
        y *= biomeFreq;

        int cellX = 0;
        int cellY = 0;

        Vec2f vec2f = null;
        int xr = NoiseUtil.round(x);
        int yr = NoiseUtil.round(y);
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        float valueDistance = 3.4028235E38F;
        DistanceFunc dist = DistanceFunc.NATURAL;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - x + vec.x;
                float vecY = yi - y + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
                    vec2f = vec;
                    cellX = xi;
                    cellY = yi;
                }

                if (distance < edgeDistance2) {
                    edgeDistance2 = Math.max(edgeDistance, distance);
                } else {
                    edgeDistance2 = Math.max(edgeDistance, edgeDistance2);
                }

                edgeDistance = Math.min(edgeDistance, distance);
            }
        }

        if (mask) {
            cell.biomeEdge = edgeValue(edgeDistance, edgeDistance2);
        } else {
            cell.biome = cellValue(seed, cellX, cellY);
            cell.biomeEdge = edgeValue(edgeDistance, edgeDistance2);
            cell.biomeMoisture = moisture.getValue(cellX + vec2f.x, cellY + vec2f.y);
            cell.biomeTemperature = temperature.getValue(cellX + vec2f.x, cellY + vec2f.y);
            cell.moisture = moisture.getValue(x, y);
            cell.temperature = temperature.getValue(x, y);

            BiomeType.apply(cell);
        }
    }

    private float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1, 1, 2);
    }

    private float edgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        value = 1 - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if (value > edgeClamp) {
            return 1F;
        }
        return value * edgeScale;
    }
}
