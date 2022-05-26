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
import com.terraforged.mod.worldgen.noise.IContinentNoise;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.continent.config.ContinentConfig;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;
import com.terraforged.noise.util.Vec2f;

public class ContinentNoise implements IContinentNoise {
    protected final TerrainLevels levels;
    protected final GeneratorContext context;
    protected final ControlPoints controlPoints;
    protected final ContinentGenerator generator;

    protected final Domain warp;
    protected final Vec2f offset;
    protected final float frequency;

    public ContinentNoise(TerrainLevels levels, GeneratorContext context) {
        this.levels = levels;
        this.context = context;
        this.controlPoints = new ControlPoints(context.settings.world.controlPoints);
        this.generator = createContinent(context, controlPoints, levels.noiseLevels);
        this.offset = generator.getWorldOffset();

        this.frequency = 1F / context.settings.world.continent.continentScale;

        double strength = 0.25;

        var builder = Source.builder()
                .octaves(2)
                .lacunarity(2.2)
                .frequency(2)
                .gain(0.3);

        this.warp = Domain.warp(
                builder.seed(context.seed.next())
                        .build(Source.PERLIN2),
                builder.seed(context.seed.next())
                        .build(Source.PERLIN2),
                Source.constant(strength)
        );
    }

    @Override
    public float getEdgeValue(float x, float y) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        px += offset.x;
        py += offset.y;

        return generator.shapeGenerator.getValue(px, py);
    }

    @Override
    public long getNearestCenter(float x, float z) {
        return 0L;
    }

    @Override
    public Rivermap getRivermap(int x, int z) {
        return null;
    }

    @Override
    public void apply(Cell cell, float x, float y) {

    }

    @Override
    public void sampleContinent(float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        px += offset.x;
        py += offset.y;

        generator.shapeGenerator.sample(px, py, sample);

        sample.terrainType = ContinentPoints.getTerrainType(sample.continentNoise);
    }

    @Override
    public void sampleRiver(float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(x, y);
        float py = warp.getY(x, y);

        px += offset.x;
        py += offset.y;

        generator.riverGenerator.sample(px, py, sample);
    }

    @Override
    public GeneratorContext getContext() {
        return context;
    }

    @Override
    public ControlPoints getControlPoints() {
        return controlPoints;
    }

    protected static ContinentGenerator createContinent(GeneratorContext context, ControlPoints controlPoints, NoiseLevels levels) {
        var config = new ContinentConfig();
        config.shape.scale = context.settings.world.continent.continentScale;
        config.shape.seed0 = context.seed.next();
        config.shape.seed1 = context.seed.next();
        return new ContinentGenerator(config, levels, controlPoints);
    }
}
