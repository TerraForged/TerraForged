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

import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.mod.worldgen.noise.IContinentNoise;
import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.continent.config.ContinentConfig;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Source;
import com.terraforged.noise.domain.Domain;

public class ContinentNoise implements IContinentNoise {
    protected final TerrainLevels levels;
    protected final GeneratorContext context;
    protected final ControlPoints controlPoints;
    protected final ContinentGenerator generator;

    protected final Domain warp;
    protected final float frequency;

    public ContinentNoise(TerrainLevels levels, GeneratorContext context) {
        this.levels = levels;
        this.context = context;
        this.controlPoints = new ControlPoints(context.settings.world.controlPoints);
        this.generator = createContinent(context, controlPoints, levels.noiseLevels);

        this.frequency = 1F / context.settings.world.continent.continentScale;

        double strength = 0.2;

        var builder = Source.builder()
                .octaves(3)
                .lacunarity(2.2)
                .frequency(3)
                .gain(0.3);

        this.warp = Domain.warp(
                builder.seed(context.seed.next()).perlin2(),
                builder.seed(context.seed.next()).perlin2(),
                Source.constant(strength)
        );
    }

    @Override
    public void sampleContinent(int seed, float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(seed, x, y);
        float py = warp.getY(seed, x, y);

        var offset = generator.getWorldOffset(seed);
        px += offset.x;
        py += offset.y;

        generator.shapeGenerator.sample(seed, px, py, sample);

        sample.terrainType = ContinentPoints.getTerrainType(sample.continentNoise);
    }

    @Override
    public void sampleRiver(int seed, float x, float y, NoiseSample sample) {
        x *= frequency;
        y *= frequency;

        float px = warp.getX(seed, x, y);
        float py = warp.getY(seed, x, y);

        var offset = generator.getWorldOffset(seed);
        px += offset.x;
        py += offset.y;

        generator.riverGenerator.sample(seed, px, py, sample);
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
