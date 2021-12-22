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

import com.terraforged.engine.settings.Settings;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.noise.erosion.ErodedNoiseGenerator;
import com.terraforged.mod.worldgen.noise.erosion.NoiseTileSize;
import com.terraforged.mod.worldgen.terrain.TerrainBlender;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

import java.util.function.Consumer;

public class NoiseGenerator implements INoiseGenerator {
    protected final float heightMultiplier = 1.2F;

    protected final long seed;
    protected final NoiseLevels levels;
    protected final Module ocean;
    protected final Module baseHeight;
    protected final TerrainBlender land;
    protected final ContinentNoise continent;
    protected final ControlPoints controlPoints;
    protected final ThreadLocal<NoiseData> localChunk = ThreadLocal.withInitial(NoiseData::new);
    protected final ThreadLocal<NoiseSample> localSample = ThreadLocal.withInitial(NoiseSample::new);

    public NoiseGenerator(long seed, TerrainLevels levels, TerrainNoise[] terrainNoises) {
        this.seed = seed;
        this.levels = levels.noiseLevels;
        this.ocean = createOceanTerrain(seed);
        this.baseHeight = createBaseTerrain(seed);
        this.land = createLandTerrain(seed, terrainNoises);
        this.continent = createContinentNoise(seed, levels);
        this.controlPoints = continent.getControlPoints();
    }

    public NoiseGenerator(long seed, TerrainLevels levels, NoiseGenerator other) {
        this.seed = seed;
        this.levels = levels.noiseLevels;
        this.land = other.land.withSeed(seed);
        this.ocean = createOceanTerrain(seed);
        this.baseHeight = createBaseTerrain(seed);
        this.continent = createContinentNoise(seed, levels);
        this.controlPoints = continent.getControlPoints();
    }

    @Override
    public NoiseGenerator with(long seed, TerrainLevels levels) {
        return new NoiseGenerator(seed, levels, this);
    }

    @Override
    public NoiseLevels getLevels() {
        return levels;
    }

    @Override
    public ContinentNoise getContinent() {
        return continent;
    }

    @Override
    public float getHeightNoise(int x, int z) {;
        return getNoiseSample(x, z).heightNoise;
    }

    @Override
    public long find(int x, int z, int minRadius, int maxRadius, Terrain terrain) {
        if (!terrain.isOverground()) return 0L;

        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        var finder = land.findNearest(nx, nz, minRadius, maxRadius, terrain);

        var sample = localSample.get();
        var riverCache = continent.getRiverCache();
        while (finder.hasNext()) {
            long pos = finder.next();
            if (pos == 0L) continue;

            float px = PosUtil.unpackLeftf(pos) / levels.frequency;
            float pz = PosUtil.unpackRightf(pos) / levels.frequency;

            // Skip if coast or ocean
            continent.sampleContinent(px, pz, sample);
            if (sample.continentNoise < controlPoints.inland) continue;

            // Skip if near river
            continent.sampleRiver(px, pz, sample, riverCache);
            if (sample.riverNoise > 0.25F) continue;

            int xi = NoiseUtil.floor(px);
            int zi = NoiseUtil.floor(pz);
            return PosUtil.pack(xi, zi);
        }

        return 0;
    }

    @Override
    public void generate(int chunkX, int chunkZ, Consumer<NoiseData> consumer) {
        var noiseData = localChunk.get();
        var blender = land.getBlenderResource();
        var riverCache = continent.getRiverCache();
        var sample = noiseData.sample;

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int dz = -1; dz < 17; dz++) {
            for (int dx = -1; dx < 17; dx++) {
                int x = startX + dx;
                int z = startZ + dz;

                sample(x, z, sample, riverCache, blender);

                noiseData.setNoise(dx, dz, sample);
            }
        }

        consumer.accept(noiseData);
    }

    public INoiseGenerator withErosion() {
        return new ErodedNoiseGenerator(seed, getNoiseTileSize(), this);
    }

    public TerrainBlender.Blender getBlenderResource() {
        return land.getBlenderResource();
    }

    public NoiseSample getNoiseSample(int x, int z) {
        var sample = localSample.get();
        var blender = land.getBlenderResource();
        var riverCache = continent.getRiverCache();
        return sample(x, z, sample, riverCache, blender);
    }

    public NoiseSample sample(int x, int z, NoiseSample sample, RiverCache riverCache, TerrainBlender.Blender blender) {
        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        sampleTerrain(nx, nz, sample, blender);
        continent.sampleRiver(nx, nz, sample, riverCache);
        return sample;
    }

    public NoiseSample sampleTerrain(float nx, float nz, NoiseSample sample, TerrainBlender.Blender blender) {
        continent.sampleContinent(nx, nz, sample);

        float continentNoise = sample.continentNoise;
        if (continentNoise < controlPoints.shallowOcean) {
            getOcean(nx, nz, sample, blender);
        } else if (continentNoise > controlPoints.inland) {
            getInland(nx, nz, sample, blender);
        } else {
            getBlend(nx, nz, sample, blender);
        }

        return sample;
    }

    public NoiseSample sampleRiver(float nx, float nz, NoiseSample sample, RiverCache riverCache) {
        continent.sampleRiver(nx, nz, sample, riverCache);
        return sample;
    }

    protected void getOcean(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float rawNoise = ocean.getValue(x, z);

        sample.heightNoise = levels.toDepthNoise(rawNoise);
        sample.terrainType = TerrainType.DEEP_OCEAN;
    }

    protected void getInland(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float baseNoise = baseHeight.getValue(x, z);
        float rawNoise = land.getValue(x, z, blender) * heightMultiplier;

        sample.heightNoise = levels.toHeightNoise(baseNoise, rawNoise);
        sample.terrainType = land.getTerrain(blender);
    }

    protected void getBlend(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float alpha = getControlAlpha(sample.continentNoise);

        float lowerRaw = ocean.getValue(x, z);
        float lower = levels.toDepthNoise(lowerRaw);

        float baseNoise = baseHeight.getValue(x, z);
        float upperRaw = land.getValue(x, z, blender) * heightMultiplier;
        float upper = levels.toHeightNoise(baseNoise, upperRaw);

        float heightNoise = NoiseUtil.lerp(lower, upper, alpha);
        sample.heightNoise = heightNoise;
        sample.terrainType = getTerrain(heightNoise, blender);
    }

    protected float getControlAlpha(float control) {
        return (control - controlPoints.shallowOcean) / (controlPoints.inland - controlPoints.shallowOcean);
    }

    protected Terrain getTerrain(float value, TerrainBlender.Blender blender) {
        if (value < levels.heightMin) return TerrainType.SHALLOW_OCEAN;

        return land.getTerrain(blender);
    }

    protected static NoiseTileSize getNoiseTileSize() {
        return new NoiseTileSize(2);
    }

    protected static Module createOceanTerrain(long seed) {
        return Source.simplex((int) seed, 64, 3).scale(0.4);
    }

    protected static Module createBaseTerrain(long seed) {
        return Source.simplex((int) seed, 200, 2);
    }

    protected static TerrainBlender createLandTerrain(long seed, TerrainNoise[] terrainNoises) {
        return new TerrainBlender(seed, 400, 0.8F, 0.275F, terrainNoises);
    }

    protected static ContinentNoise createContinentNoise(long seed, TerrainLevels levels) {
        var settings = new Settings();
        settings.world.seed = seed;
        settings.world.properties.seaLevel = levels.seaLevel;
        settings.world.properties.worldHeight = levels.maxY;
        settings.rivers.riverCount = 7;

        var context = new GeneratorContext(settings);
        return new ContinentNoise(context.seed, context);
    }
}
