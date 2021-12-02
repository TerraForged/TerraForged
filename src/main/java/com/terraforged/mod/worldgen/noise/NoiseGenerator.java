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
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.heightmap.ControlPoints;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.asset.TerrainConfig;
import com.terraforged.mod.worldgen.terrain.TerrainBlender;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;

import java.util.Map;

public class NoiseGenerator {
    public static final float SCALE = 1.15F;

    protected final NoiseLevels levels;
    protected final Module ocean;
    protected final TerrainBlender land;
    protected final ContinentNoise continent;
    protected final ControlPoints controlPoints;
    protected final ThreadLocal<NoiseData> localData = ThreadLocal.withInitial(NoiseData::new);

    public NoiseGenerator(long seed, TerrainLevels levels, RegistryAccess access) {
        this.levels = levels.noiseLevels;
        this.ocean = createOceanTerrain(seed);
        this.land = createLandTerrain(seed, access);
        this.continent = createContinentNoise(seed, levels);
        this.controlPoints = continent.getControlPoints();
    }

    public NoiseGenerator(long seed, TerrainLevels levels, NoiseGenerator other) {
        this.levels = levels.noiseLevels;
        this.land = other.land.withSeed(seed);
        this.ocean = createOceanTerrain(seed);
        this.continent = createContinentNoise(seed, levels);
        this.controlPoints = continent.getControlPoints();
    }

    public float getHeightNoise(int x, int z) {;
        return getNoiseSample(x, z).heightNoise;
    }

    public ContinentNoise getContinent() {
        return continent;
    }

    public NoiseSample getNoiseSample(int x, int z) {
        var sample = localData.get().getSample();
        var blender = land.getBlenderResource();
        var riverCache = continent.getRiverCache();
        return sample(x, z, sample, riverCache, blender);
    }

    public NoiseData generate(ChunkPos pos) {
        int x = pos.getMinBlockX();
        int z = pos.getMinBlockZ();

        var noiseData = localData.get();
        var sample = noiseData.getSample();
        var riverCache = continent.getRiverCache();
        var blender = land.getBlenderResource();

        int min = noiseData.min();
        int max = noiseData.max();
        for (int dz = min; dz < max; dz++) {
            for (int dx = min; dx < max; dx++) {
                int px = x + dx;
                int pz = z + dz;
                sample(px, pz, sample, riverCache, blender);

                noiseData.setNoise(dx, dz, sample(px, pz, sample, riverCache, blender));
            }
        }

        return noiseData;
    }

    public NoiseSample sample(int x, int z, NoiseSample sample, RiverCache riverCache, TerrainBlender.Blender blender) {
        float nx = x * levels.frequency;
        float nz = z * levels.frequency;

        continent.sampleContinent(nx, nz, sample);

        float continentNoise = sample.continentNoise;
        if (continentNoise < controlPoints.shallowOcean) {
            getOcean(nx, nz, sample, blender);
        } else if (continentNoise > controlPoints.inland) {
            getInland(nx, nz, sample, blender);
        } else {
            getBlend(nx, nz, sample, blender);
        }

        continent.sampleRiver(nx, nz, sample, riverCache);

        return sample;
    }

    protected void getOcean(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float rawNoise = ocean.getValue(x, z);

        sample.heightNoise = levels.toDepthNoise(rawNoise);
        sample.terrainType = TerrainType.DEEP_OCEAN;
    }

    protected void getInland(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float rawNoise = land.getValue(x, z, blender) * SCALE;

        sample.heightNoise = levels.toHeightNoise(rawNoise);
        sample.terrainType = land.getTerrain(blender);
    }

    protected void getBlend(float x, float z, NoiseSample sample, TerrainBlender.Blender blender) {
        float alpha = getControlAlpha(sample.continentNoise);

        float lowerRaw = ocean.getValue(x, z);
        float lower = levels.toDepthNoise(lowerRaw);

        float upperRaw = land.getValue(x, z, blender) * SCALE;
        float upper = levels.toHeightNoise(upperRaw);

        float heightNoise = NoiseUtil.lerp(lower, upper, alpha);
        sample.heightNoise = heightNoise;
        sample.terrainType = getTerrain(heightNoise, blender);
    }

    protected float getControlAlpha(float control) {
        return (control - controlPoints.shallowOcean) / (controlPoints.inland - controlPoints.shallowOcean);
    }

    protected Terrain getTerrain(float value, TerrainBlender.Blender blender) {
        if (value <= levels.heightMin) return TerrainType.DEEP_OCEAN;

        return land.getTerrain(blender);
    }

    protected static Module createOceanTerrain(long seed) {
        return Source.simplex((int) seed, 64, 3).scale(0.4);
    }

    protected static TerrainBlender createLandTerrain(long seed, RegistryAccess access) {
        return new TerrainBlender(seed, 400, 0.8F, 0.65F, getTerrain(access));
    }

    protected static ContinentNoise createContinentNoise(long seed, TerrainLevels levels) {
        var settings = new Settings();
        settings.world.seed = seed;
        settings.world.properties.seaLevel = levels.seaLevel;
        settings.world.properties.worldHeight = levels.genDepth;
        settings.rivers.riverCount = 7;

        var context = new GeneratorContext(settings);

        return new ContinentNoise(context.seed, context);
    }

    protected static TerrainConfig[] getTerrain(RegistryAccess access) {
        var registry = access.registryOrThrow(ModRegistry.TERRAIN);
        return registry.entrySet().stream()
                .map(Map.Entry::getValue)
                .toArray(TerrainConfig[]::new);
    }
}
