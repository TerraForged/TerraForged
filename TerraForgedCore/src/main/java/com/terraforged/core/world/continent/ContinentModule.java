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

package com.terraforged.core.world.continent;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.cell.Populator;
import com.terraforged.core.settings.GeneratorSettings;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.domain.Domain;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.func.EdgeFunc;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;

public class ContinentModule implements Populator {

    private static final float edgeClampMin = 0.05F;
    private static final float edgeClampMax = 0.50F;
    private static final float edgeClampRange = edgeClampMax - edgeClampMin;

    private final int seed;
    private final float frequency;

    private final float edgeMin;
    private final float edgeMax;
    private final float edgeRange;

    private final Domain warp;
    private final Module shape;

    public ContinentModule(Seed seed, GeneratorSettings settings) {
        int tectonicScale = settings.land.continentScale * 4;
        int continentScale = settings.land.continentScale / 2;
        double oceans = Math.min(Math.max(settings.world.oceanSize, 0.01), 0.99);
        double shapeMin = 0.15 + (oceans * 0.35);
        this.seed = seed.next();

        this.frequency = 1F / tectonicScale;
        this.edgeMin = edgeClampMin;
        this.edgeMax = (float) oceans;
        this.edgeRange = edgeMax - edgeMin;

        this.warp = Domain.warp(Source.SIMPLEX, seed.next(), continentScale, 3, continentScale);

        this.shape = Source.perlin(seed.next(), settings.land.continentScale, 2)
                .clamp(shapeMin, 0.7)
                .map(0, 1)
                .warp(Source.SIMPLEX, seed.next(), continentScale / 2, 1, continentScale / 4D)
                .warp(seed.next(), 50, 1, 20D);
    }

    @Override
    public float getValue(float x, float y) {
        if (true) {
            throw new RuntimeException("no pls!");
        } else {
            Cell<Terrain> cell = new Cell<>();
            apply(cell, x, y);
            return cell.continentEdge;
        }
    }

    @Override
    public void apply(Cell<Terrain> cell, final float x, final float y) {
        float ox = warp.getOffsetX(x, y);
        float oz = warp.getOffsetY(x, y);

        float px = x + ox;
        float py = y + oz;

        px *= frequency;
        py *= frequency;

        int cellX = 0;
        int cellY = 0;

        int xr = NoiseUtil.round(px);
        int yr = NoiseUtil.round(py);
        float edgeDistance = 999999.0F;
        float edgeDistance2 = 999999.0F;
        float valueDistance = 3.4028235E38F;
        DistanceFunc dist = DistanceFunc.NATURAL;

        for (int dy = -1; dy <= 1; dy++) {
            for (int dx = -1; dx <= 1; dx++) {
                int xi = xr + dx;
                int yi = yr + dy;
                Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, xi, yi) & 255];

                float vecX = xi - px + vec.x;
                float vecY = yi - py + vec.y;
                float distance = dist.apply(vecX, vecY);

                if (distance < valueDistance) {
                    valueDistance = distance;
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


        float shapeNoise = shape.getValue(x, y);
        float continentNoise = edgeValue(edgeDistance, edgeDistance2);

        cell.continent = cellValue(seed, cellX, cellY);
        cell.continentEdge = shapeNoise * continentNoise;
    }

    @Override
    public void tag(Cell<Terrain> cell, float x, float y) {

    }

    private float cellValue(int seed, int cellX, int cellY) {
        float value = NoiseUtil.valCoord2D(seed, cellX, cellY);
        return NoiseUtil.map(value, -1, 1, 2);
    }

    private float edgeValue(float distance, float distance2) {
        EdgeFunc edge = EdgeFunc.DISTANCE_2_DIV;
        float value = edge.apply(distance, distance2);
        float edgeValue = 1 - NoiseUtil.map(value, edge.min(), edge.max(), edge.range());
        if (edgeValue < edgeMin) {
            return 0F;
        }
        if (edgeValue > edgeMax) {
            return 1F;
        }
        return (edgeValue - edgeMin) / edgeRange;
    }
}
