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

import com.terraforged.engine.settings.FilterSettings;
import com.terraforged.mod.worldgen.noise.filter.ErosionFilter;
import com.terraforged.mod.worldgen.noise.filter.NoiseResource;
import com.terraforged.mod.worldgen.terrain.TerrainBlender;

public class ErodeNoiseGenerator {
    protected final ErosionFilter erosion;
    protected final NoiseGenerator generator;

    public ErodeNoiseGenerator(long seed, NoiseGenerator generator) {
        this.generator = generator;
        var settings = new FilterSettings.Erosion();
        settings.dropletsPerChunk = 250;
        this.erosion = new ErosionFilter((int) seed, NoiseResource.REGION_SIZE, settings);
    }

    public void generate(int chunkX, int chunkZ, NoiseResource resource, TerrainBlender.Blender blender) {
        generateHeightmap(chunkX, chunkZ, resource, blender);
        generateErosion(chunkX, chunkZ, resource);
        generateRivers(chunkX, chunkZ, resource);
    }

    protected void generateHeightmap(int chunkX, int chunkZ, NoiseResource resource, TerrainBlender.Blender blender) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;
        for (int dz = NoiseResource.REL_MIN; dz < NoiseResource.REL_MAX; dz++) {
            float nz = getCoord(startZ + dz);

            for (int dx = NoiseResource.REL_MIN; dx < NoiseResource.REL_MAX; dx++) {
                float nx = getCoord(startX + dx);

                int rx = NoiseResource.CHUNK_SIZE + dx;
                int rz = NoiseResource.CHUNK_SIZE + dz;
                int heightIndex = NoiseResource.indexOf(rx, rz);

                var sample = resource.getSample(dx, dz);
                generator.sampleTerrain(nx, nz, sample, blender);

                resource.heightmap[heightIndex] = sample.heightNoise;
            }
        }
    }

    protected void generateErosion(int chunkX, int chunkZ, NoiseResource resource) {
        erosion.apply(resource.heightmap, chunkX, chunkZ, NoiseResource.REGION_SIZE);
    }

    protected void generateRivers(int chunkX, int chunkZ, NoiseResource resource) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        int min = resource.chunk.min();
        int max = resource.chunk.max();
        for (int dz = min; dz < max; dz++) {
            float nz = getCoord(startZ + dz);
            int rz = NoiseResource.CHUNK_SIZE + dz;

            for (int dx = min; dx < max; dx++) {
                float nx = getCoord(startX + dx);
                int rx = NoiseResource.CHUNK_SIZE + dx;

                int heightIndex = NoiseResource.indexOf(rx, rz);
                float height = resource.heightmap[heightIndex];

                int sampleIndex = resource.chunk.index().of(dx, dz);
                var sample = resource.chunkSample.get(sampleIndex);
                sample.heightNoise = height;

                generator.sampleRiver(nx, nz, sample, resource.riverCache);

                resource.chunk.setNoise(sampleIndex, sample);
            }
        }
    }

    protected float getCoord(int c) {
        return c * generator.getLevels().frequency;
    }
}
