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
import com.terraforged.mod.worldgen.noise.continent.ContinentGenerator;
import com.terraforged.mod.worldgen.noise.continent.ContinentNoise;
import com.terraforged.mod.worldgen.noise.continent.ContinentPoints;
import com.terraforged.mod.worldgen.noise.erosion.ErodedNoiseGenerator;
import com.terraforged.mod.worldgen.noise.erosion.NoiseTileSize;
import com.terraforged.mod.worldgen.terrain.TerrainBlender;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

import java.util.function.Consumer;

public class NoiseGenerator implements INoiseGenerator {
    protected static final int OCEAN_OFFSET = 8763214;
    protected static final int TERRAIN_OFFSET = 45763218;
    protected static final int CONTINENT_OFFSET = 18749560;
    protected final float heightMultiplier = 1.2F;

    protected final TerrainLevels levels;
    protected final Module ocean;
    protected final TerrainBlender land;
    protected final IContinentNoise continent;
    protected final ControlPoints controlPoints;
    protected final ThreadLocal<NoiseData> localChunk = ThreadLocal.withInitial(NoiseData::new);
    protected final ThreadLocal<NoiseSample> localSample = ThreadLocal.withInitial(NoiseSample::new);

    public NoiseGenerator(TerrainLevels levels, TerrainNoise[] terrainNoises) {
        this.levels = levels;
        this.ocean = createOceanTerrain();
        this.land = createLandTerrain(terrainNoises);
        this.continent = createContinentNoise(levels);
        this.controlPoints = continent.getControlPoints();
    }

    public NoiseGenerator(TerrainLevels levels, NoiseGenerator other) {
        this.levels = levels;
        this.land = other.land;
        this.ocean = other.ocean;
        this.continent = other.continent;
        this.controlPoints = continent.getControlPoints();
    }

    @Override
    public NoiseLevels getLevels() {
        return levels.noiseLevels;
    }

    @Override
    public TerrainLevels getTerrainLevels() {
        return levels;
    }

    @Override
    public IContinentNoise getContinent() {
        return continent;
    }

    @Override
    public float getHeightNoise(int seed, int x, int z) {;
        return getNoiseSample(seed, x, z).heightNoise;
    }

    @Override
    public long find(int seed, int x, int z, int minRadius, int maxRadius, Terrain terrain) {
        if (!terrain.isOverground()) return 0L;

        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        var finder = land.findNearest(seed, nx, nz, minRadius, maxRadius, terrain);

        var sample = localSample.get().reset();
        while (finder.hasNext()) {
            long pos = finder.next();
            if (pos == 0L) continue;

            float px = PosUtil.unpackLeftf(pos) / levels.noiseLevels.frequency;
            float pz = PosUtil.unpackRightf(pos) / levels.noiseLevels.frequency;

            // Skip if coast or ocean
            continent.sampleContinent(seed, px, pz, sample);
            if (sample.continentNoise < ContinentPoints.BEACH) continue;

            // Skip if near river
            continent.sampleRiver(seed, px, pz, sample);
            if (!terrain.isRiver() && sample.riverNoise < 0.75F) continue;

            int xi = NoiseUtil.floor(px);
            int zi = NoiseUtil.floor(pz);
            return PosUtil.pack(xi, zi);
        }

        return 0;
    }

    @Override
    public void generate(int seed, int chunkX, int chunkZ, Consumer<NoiseData> consumer) {
        var noiseData = localChunk.get();
        var blender = land.getBlenderResource();
        var sample = noiseData.sample;

        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int dz = -1; dz < 17; dz++) {
            for (int dx = -1; dx < 17; dx++) {
                int x = startX + dx;
                int z = startZ + dz;

                sample(seed, x, z, sample, blender);

                noiseData.setNoise(dx, dz, sample);
            }
        }

        consumer.accept(noiseData);
    }

    public INoiseGenerator withErosion() {
        return new ErodedNoiseGenerator(getNoiseTileSize(), this);
    }

    public TerrainBlender.Blender getBlenderResource() {
        return land.getBlenderResource();
    }

    @Override
    public NoiseSample getNoiseSample(int seed, int x, int z) {
        var sample = localSample.get().reset();
        sample(seed, x, z, sample);
        return sample;
    }

    @Override
    public void sample(int seed, int x, int z, NoiseSample sample) {
        var blender = land.getBlenderResource();
        sample(seed, x, z, sample, blender);
    }

    public void sampleContinentNoise(int seed, int x, int z, NoiseSample sample) {
        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        continent.sampleContinent(seed, nx, nz, sample);
    }

    public void sampleRiverNoise(int seed, int x, int z, NoiseSample sample) {
        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        continent.sampleRiver(seed, nx, nz, sample);
    }

    public NoiseSample sample(int seed, int x, int z, NoiseSample sample, TerrainBlender.Blender blender) {
        float nx = getNoiseCoord(x);
        float nz = getNoiseCoord(z);
        sampleTerrain(seed, nx, nz, sample, blender);
        sampleRiver(seed, nx, nz, sample);
        return sample;
    }

    public NoiseSample sampleTerrain(int seed, float nx, float nz, NoiseSample sample, TerrainBlender.Blender blender) {
        continent.sampleContinent(seed, nx, nz, sample);

        float continentNoise = sample.continentNoise;
        if (continentNoise < ContinentPoints.SHALLOW_OCEAN) {
            getOcean(seed, nx, nz, sample, blender);
        } else if (continentNoise < ContinentPoints.COAST) {
            getBlend(seed, nx, nz, sample, blender);
        } else {
            getInland(seed, nx, nz, sample, blender);
        }

        return sample;
    }

    public NoiseSample sampleRiver(int seed, float nx, float nz, NoiseSample sample) {
        continent.sampleRiver(seed, nx, nz, sample);
        return sample;
    }

    protected void getOcean(int seed, float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float rawNoise = ocean.getValue(seed, x, z);

        sample.heightNoise = levels.noiseLevels.toDepthNoise(rawNoise);
        sample.terrainType = TerrainType.DEEP_OCEAN;
    }

    protected void getInland(int seed, float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float baseNoise = sample.baseNoise;
        float heightNoise = land.getValue(seed, x, z, blender) * heightMultiplier;

        sample.heightNoise = levels.noiseLevels.toHeightNoise(baseNoise, heightNoise);
        sample.terrainType = land.getTerrain(blender);
    }

    protected void getBlend(int seed, float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        if (sample.continentNoise < ContinentPoints.BEACH) {
            float lowerRaw = ocean.getValue(seed, x, z);
            float lower = levels.noiseLevels.toDepthNoise(lowerRaw);

            float upper = levels.noiseLevels.heightMin;
            float alpha = (sample.continentNoise - ContinentPoints.SHALLOW_OCEAN) / (ContinentPoints.BEACH - ContinentPoints.SHALLOW_OCEAN);

            sample.heightNoise = NoiseUtil.lerp(lower, upper, alpha);
        } else if (sample.continentNoise < ContinentPoints.COAST) {
            float lower = levels.noiseLevels.heightMin;

            float baseNoise = sample.baseNoise;
            float upperRaw = land.getValue(seed, x, z, blender) * heightMultiplier;
            float upper = levels.noiseLevels.toHeightNoise(baseNoise, upperRaw);

            float alpha = (sample.continentNoise - ContinentPoints.BEACH) / (ContinentPoints.COAST - ContinentPoints.BEACH);

            sample.heightNoise = NoiseUtil.lerp(lower, upper, alpha);
            sample.terrainType = land.getTerrain(blender);
        }
    }

    protected Terrain getTerrain(float value, TerrainBlender.Blender blender) {
        if (value < levels.noiseLevels.heightMin) return TerrainType.SHALLOW_OCEAN;

        return land.getTerrain(blender);
    }

    protected static NoiseTileSize getNoiseTileSize() {
        return new NoiseTileSize(2);
    }

    protected static Module createOceanTerrain() {
        return Source.simplex(OCEAN_OFFSET, 64, 3).scale(0.4);
    }

    protected static TerrainBlender createLandTerrain(TerrainNoise[] terrainNoises) {
        return new TerrainBlender(TERRAIN_OFFSET, 800, 0.8F, 0.4F /*0.275F*/, terrainNoises);
    }

    protected static IContinentNoise createContinentNoise(TerrainLevels levels) {
        var settings = new Settings();
        settings.world.seed = CONTINENT_OFFSET;
        settings.world.properties.seaLevel = levels.seaLevel;
        settings.world.properties.worldHeight = levels.maxY;

        settings.climate.biomeShape.biomeSize = 220;
        settings.climate.temperature.falloff = 2;
        settings.climate.temperature.bias = 0.1f;
        settings.climate.moisture.falloff = 1;
        settings.climate.moisture.bias = -0.05f;

        var context = new GeneratorContext(settings);

        settings.world.continent.continentScale = ContinentGenerator.CONTINENT_SAMPLE_SCALE;
        settings.world.controlPoints.deepOcean = 0.05f;
        settings.world.controlPoints.shallowOcean = 0.3f;
        settings.world.controlPoints.beach = 0.45f;
        settings.world.controlPoints.coast = 0.75f;
        settings.world.controlPoints.inland = 0.80f;
        return new ContinentNoise(levels, context);
    }
}
