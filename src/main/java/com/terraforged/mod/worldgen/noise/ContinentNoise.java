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

package com.terraforged.mod.worldgen.noise;

import com.terraforged.engine.Seed;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.continent.advanced.AdvancedContinentGenerator;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.noise.source.Line;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2f;

public class ContinentNoise extends AdvancedContinentGenerator {
    protected final float offsetX;
    protected final float offsetZ;
    protected final GeneratorContext context;
    protected final ThreadLocal<RiverCache> localRiverCache = ThreadLocal.withInitial(RiverCache::new);

    public ContinentNoise(Seed seed, GeneratorContext context) {
        super(seed, context);
        var centre = NoiseUtil.cell(this.seed, 0, 0);
        this.context = context;
        this.offsetX = (centre.x  * jitter) / frequency;
        this.offsetZ = (centre.y  * jitter) / frequency;
    }

    public GeneratorContext getContext() {
        return context;
    }

    public RiverCache getRiverCache() {
        return localRiverCache.get();
    }

    public ControlPoints getControlPoints() {
        return controlPoints;
    }

    public void sampleContinent(float x, float y, NoiseSample sample) {
        x += offsetX;
        y += offsetZ;

        float wx = warp.getX(x, y);
        float wy = warp.getY(x, y);
        x = wx * frequency;
        y = wy * frequency;

        int xi = NoiseUtil.floor(x);
        int yi = NoiseUtil.floor(y);

        int cellX = xi;
        int cellY = yi;
        float cellPointX = x;
        float cellPointY = y;
        float nearest = Float.MAX_VALUE;

        for (int cy = yi - 1; cy <= yi + 1; cy++) {
            for (int cx = xi - 1; cx <= xi + 1; cx++) {
                Vec2f vec = NoiseUtil.cell(seed, cx, cy);

                float px = cx + vec.x * jitter;
                float py = cy + vec.y * jitter;
                float dist2 = Line.dist2(x, y, px, py);

                if (dist2 < nearest) {
                    cellPointX = px;
                    cellPointY = py;
                    cellX = cx;
                    cellY = cy;
                    nearest = dist2;
                }
            }
        }

        nearest = Float.MAX_VALUE;

        float sumX = 0;
        float sumY = 0;
        for (int cy = cellY - 1; cy <= cellY + 1; cy++) {
            for (int cx = cellX - 1; cx <= cellX + 1; cx++) {
                if (cx == cellX && cy == cellY) {
                    continue;
                }

                Vec2f vec = NoiseUtil.cell(seed, cx, cy);
                float px = cx + vec.x * jitter;
                float py = cy + vec.y * jitter;
                float dist2 = getDistance(x, y, cellPointX, cellPointY, px, py);

                sumX += px;
                sumY += py;

                if (dist2 < nearest) {
                    nearest = dist2;
                }
            }
        }

        if (shouldSkip(cellX, cellY)) {
            sample.continentNoise = -1;
            sample.continentCentre = 0;
            return;
        }

        int continentX = getCorrectedContinentCentre(cellPointX, sumX / 8);
        int continentZ = getCorrectedContinentCentre(cellPointY, sumY / 8);

        sample.continentNoise = getDistanceValue(x, y, cellX, cellY, nearest);
        sample.continentCentre = PosUtil.pack(continentX, continentZ);
    }

    public void sampleRiver(float x, float z, NoiseSample sample, RiverCache cache) {
        if (sample.continentNoise <= 0)
            return;

        x += offsetX;
        z += offsetZ;

        var cell = cache.cell.reset();
        cell.value = sample.heightNoise;
        cell.terrain = sample.terrainType;
        cell.continentEdge = sample.continentNoise;

        cache.get(sample.continentCentre, this).apply(cell, x, z);

        sample.heightNoise = cell.value;
        sample.terrainType = cell.terrain;
        sample.riverNoise = cell.riverMask;
    }
}
