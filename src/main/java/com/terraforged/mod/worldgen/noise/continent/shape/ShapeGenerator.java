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

package com.terraforged.mod.worldgen.noise.continent.shape;

import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.continent.ContinentConfig;
import com.terraforged.mod.worldgen.noise.continent.ContinentGenerator;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;
import com.terraforged.mod.worldgen.noise.continent.cell.CellPoint;
import com.terraforged.noise.util.NoiseUtil;

public class ShapeGenerator {
    private static final int RADIUS = 2;

    private final float baseFalloff;
    private final float continentFalloff;

    public final float threshold;
    public final float thresholdMax;

    private final ContinentGenerator continent;
    private final FalloffPoint[] falloffPoints;
    private final ThreadLocal<long[]> edgeBuffer = ThreadLocal.withInitial(() -> new long[9]);
    private final ThreadLocal<CellLocal[]> cellBuffer = ThreadLocal.withInitial(CellLocal::init);

    public ShapeGenerator(ContinentGenerator continent, ContinentConfig config, ControlPoints controlPoints) {
        this.continent = continent;
        this.baseFalloff = config.noise.baseNoiseFalloff;
        this.continentFalloff = config.noise.continentNoiseFalloff;
        this.falloffPoints = ContinentPoints.getFalloff(controlPoints);
        this.threshold = config.shape.threshold;
        this.thresholdMax = config.shape.getThresholdMax();
    }

    public float getThresholdValue(CellPoint cell) {
        return cell.noise < threshold ? 0f : 1f;
    }

    public float getFalloff(float continentNoise) {
        return ContinentPoints.getFalloff(continentNoise, falloffPoints);
    }

    public float getBaseNoise(float value) {
        float min = threshold;
        float max = thresholdMax;
        return NoiseUtil.map(value, min, max, max - min);
    }

    public float getValue(float x, float y) {
        var centre = continent.getNearestCell(x, y);
        int centreX = PosUtil.unpackLeft(centre);
        int centreY = PosUtil.unpackRight(centre);

        x = continent.cellShape.adjustX(x);
        y = continent.cellShape.adjustY(y);

        int minX = centreX - RADIUS;
        int minY = centreY - RADIUS;
        int maxX = centreX + RADIUS;
        int maxY = centreY + RADIUS;

        float min0 = Float.MAX_VALUE;
        float min1 = Float.MAX_VALUE;
        var data = edgeBuffer.get();

        for (int cy = minY, i = 0; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++, i++) {
                var cell = continent.getCell(cx, cy);

                float value = getThresholdValue(cell);
                float distance = NoiseUtil.sqrt(NoiseUtil.dist2(x, y, cell.px, cell.py));

                data[i] = PosUtil.packf(value, distance);

                if (distance < min0) {
                    min1 = min0;
                    min0 = distance;
                } else if (distance < min1) {
                    min1 = distance;
                }
            }
        }

        return getFalloff(getEdge(min0, min1, continentFalloff, data));
    }

    public NoiseSample sample(float x, float y, NoiseSample sample) {
        var centre = continent.getNearestCell(x, y);
        int centreX = PosUtil.unpackLeft(centre);
        int centreY = PosUtil.unpackRight(centre);

        // Note: Must adjust inputs AFTER getting nearest cell
        x = continent.cellShape.adjustX(x);
        y = continent.cellShape.adjustY(y);

        int minX = centreX - RADIUS;
        int minY = centreY - RADIUS;
        int maxX = centreX + RADIUS;
        int maxY = centreY + RADIUS;

        int closest = -1;
        float min0 = Float.MAX_VALUE;
        float min1 = Float.MAX_VALUE;
        var buffer = cellBuffer.get();

        for (int cy = minY, i = 0; cy <= maxY; cy++) {
            for (int cx = minX; cx <= maxX; cx++, i++) {
                var cell = continent.getCell(cx, cy);
                var local = buffer[i];

                float distance = NoiseUtil.sqrt(NoiseUtil.dist2(x, y, cell.px, cell.py));

                local.cell = cell;
                local.context = distance;

                if (distance < min0) {
                    min1 = min0;
                    min0 = distance;
                    closest = i;
                } else if (distance < min1) {
                    min1 = distance;
                }
            }
        }

        return sampleEdges(closest, min0, min1, buffer, sample);
    }

    private float getEdge(float min0, float min1, float falloff, long[] data) {
        float borderDistance = (min0 + min1) * 0.5F;
        float blendRadius = borderDistance * falloff;

        float sumValue = 0f;
        float sumWeight = 0f;

        for (long l : data) {
            float value = PosUtil.unpackLeftf(l);
            float distance = PosUtil.unpackRightf(l);
            float weight = getWeight(distance, min0, blendRadius);

            sumValue += value * weight;
            sumWeight += weight;
        }

        return NoiseUtil.clamp(sumValue / sumWeight, 0, 1);
    }

    private NoiseSample sampleEdges(int index, float min0, float min1, CellLocal[] buffer, NoiseSample sample) {
        float borderDistance = (min0 + min1) * 0.5F;
        float baseBlend = borderDistance * baseFalloff;
        float continentBlend = borderDistance * continentFalloff;

        float sumBase = 0f;
        float sumContinent = 0f;

        float sumBaseWeight = 0f;
        float sumContinentWeight = 0f;

        for (var local : buffer) {
            float dist = local.context;
            float baseValue = local.cell.noise();
            float continentValue = getThresholdValue(local.cell);

            float baseWeight = getWeight(dist, min0, baseBlend);
            float continentWeight = getWeight(dist, min0, continentBlend);

            sumBase += baseValue * baseWeight;
            sumContinent += continentValue * continentWeight;

            sumBaseWeight += baseWeight;
            sumContinentWeight += continentWeight;
        }

        sample.baseNoise = getBaseNoise(sumBase / sumBaseWeight);
        sample.continentNoise = getFalloff(sumContinent / sumContinentWeight);

        return sample;
    }

    private static float getWeight(float dist, float origin, float blendRange) {
        float delta = dist - origin;
        if (delta <= 0) return 1F;
        if (delta >= blendRange) return 0F;
        return 1 - (delta / blendRange);
    }

    protected static class CellLocal {
        public CellPoint cell;
        public float context;

        protected static CellLocal[] init() {
            int size = 1 + RADIUS * 2;
            var cells = new CellLocal[size * size];
            for (int i = 0; i < cells.length; i++) {
                cells[i] = new CellLocal();
            }
            return cells;
        }
    }
}
