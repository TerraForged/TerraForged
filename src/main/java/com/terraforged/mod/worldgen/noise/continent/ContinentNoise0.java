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

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.engine.world.rivermap.Rivermap;
import com.terraforged.engine.world.rivermap.gen.GenWarp;
import com.terraforged.engine.world.rivermap.river.Network;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.worldgen.noise.IContinentNoise;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.RiverCache;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.NoiseUtil;

public class ContinentNoise0 implements IContinentNoise {
    private static final RiverCache NO_CACHE = new RiverCache();
    private static final Rivermap NO_RIVER_MAP = new Rivermap(0, 0, new Network[0], GenWarp.EMPTY);

    protected final GeneratorContext context;
    protected final ControlPoints controlPoints;
    protected final ContinentGenerator generator;

    protected final Domain warp;
    protected final float frequency;

    public ContinentNoise0(GeneratorContext context) {
        this.context = context;
        this.controlPoints = new ControlPoints(context.settings.world.controlPoints);
        this.generator = new ContinentGenerator(context.seed.get(), 0.7f, 0.35f, Source.ZERO);

        this.frequency = 1F / context.settings.world.continent.continentScale;

        int scale = 150;
        int octaves = 3;
        double gain = 0.3;
        double lacunarity = 2.8;
        double strength = 100;

        warp = Domain.warp(
                Source.build(context.seed.next(), scale, octaves)
                        .gain(gain)
                        .lacunarity(lacunarity)
                        .build(Source.SIMPLEX2),
                Source.build(context.seed.next(), scale, octaves)
                        .gain(gain)
                        .lacunarity(lacunarity)
                        .build(Source.SIMPLEX2),
                Source.constant(strength)
        );
    }

    @Override
    public float getEdgeValue(float x, float z) {
        float px = warp.getX(x, z);
        float py = warp.getY(x, z);

        px *= frequency;
        py *= frequency;

        return generator.getEdgeValue(px, py);
    }

    @Override
    public long getNearestCenter(float x, float z) {
        float px = warp.getX(x, z);
        float py = warp.getY(x, z);

        px *= frequency;
        py *= frequency;

        return generator.getNearest(px, py);
    }

    @Override
    public Rivermap getRivermap(int x, int z) {
        return NO_RIVER_MAP;
    }

    @Override
    public void apply(Cell cell, float x, float y) {

    }

    @Override
    public void sampleContinent(float x, float y, NoiseSample sample) {
        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        px *= frequency;
        py *= frequency;

        sample.continentNoise = generator.getEdgeValue(px, py);
        sample.terrainType = getTerrainType(sample);
    }

    @Override
    public void sampleRiver(float x, float y, NoiseSample sample, RiverCache cache) {
        if (sample.continentNoise <= 0) return;
        if (true) return;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        px *= frequency;
        py *= frequency;

        float height = sample.heightNoise;
        float river = NoiseUtil.sqrt(generator.getRiverValue(px, py));

        float banks = context.levels.water(2);
        float bed = context.levels.water(-3);
        float bankWidth = 0.1f;
        float bedWidth = 0.025f;

        if (river < 1 && banks < height) {
            float alpha = NoiseUtil.map(river, bankWidth, 1.0f, 1.0f - bankWidth);
            height = NoiseUtil.lerp(banks, height, alpha);
        }

        if (river < bankWidth && bed < height) {
            float alpha = NoiseUtil.map(river, bedWidth, bankWidth, bankWidth - bedWidth);
            height = NoiseUtil.lerp(bed, height, alpha);
        }

        sample.riverNoise = river;
        sample.heightNoise = height;
    }

    @Override
    public GeneratorContext getContext() {
        return context;
    }

    @Override
    public RiverCache getRiverCache() {
        return NO_CACHE;
    }

    @Override
    public ControlPoints getControlPoints() {
        return controlPoints;
    }

    private Terrain getTerrainType(NoiseSample sample) {
        if (sample.continentNoise < controlPoints.shallowOcean) {
            return TerrainType.DEEP_OCEAN;
        }
        if (sample.continentNoise < controlPoints.beach) {
            return TerrainType.SHALLOW_OCEAN;
        }
        if (sample.continentNoise < controlPoints.coastMarker) {
            return TerrainType.COAST;
        }
        return TerrainType.NONE;
    }
}
