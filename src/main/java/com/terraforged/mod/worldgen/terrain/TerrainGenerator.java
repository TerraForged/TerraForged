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

package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.world.level.ChunkPos;

public class TerrainGenerator {
    protected final TerrainLevels levels;
    protected final NoiseGenerator noiseGenerator;
    protected final ObjectPool<TerrainData> terrainDataPool;

    public TerrainGenerator(NoiseGenerator generator) {
        this(TerrainLevels.DEFAULT, generator);
    }

    public TerrainGenerator(TerrainLevels levels, NoiseGenerator noiseGenerator) {
        this.levels = levels;
        this.noiseGenerator = noiseGenerator;
        this.terrainDataPool = new ObjectPool<>(() -> new TerrainData(this.levels));
    }

    public TerrainGenerator withNoise(NoiseGenerator generator) {
        return new TerrainGenerator(levels, generator);
    }

    public NoiseGenerator getNoiseGenerator() {
        return noiseGenerator;
    }

    public void restore(TerrainData terrainData) {
        terrainDataPool.restore(terrainData);
    }

    public TerrainData generate(ChunkPos chunkPos) {
        var noiseData = noiseGenerator.generate(chunkPos);
        var terrainData = terrainDataPool.take();
        terrainData.assign(noiseData);
        return terrainData;
    }

    public int getHeight(int x, int z) {
        float heightNoise = noiseGenerator.getHeightNoise(x, z);
        float scaledHeight = levels.getScaledHeight(heightNoise);
        return levels.getHeight(scaledHeight);
    }
}
