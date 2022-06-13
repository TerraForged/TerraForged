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

package com.terraforged.mod.worldgen.noise.erosion;

import com.terraforged.engine.util.FastRandom;
import com.terraforged.mod.util.storage.ObjectMap;
import com.terraforged.mod.worldgen.noise.NoiseData;
import com.terraforged.mod.worldgen.noise.NoiseSample;

import java.util.concurrent.CompletableFuture;

public class NoiseResource {
    public final FastRandom random = new FastRandom();

    public final NoiseData chunk = new NoiseData();
    public final ErosionFilter.Resource erosionResource = new ErosionFilter.Resource();

    public final float[] heightmap;
    public final NoiseSample sharedSample;
    public final ObjectMap<NoiseSample> chunkSample;

    public final CompletableFuture<float[]>[] chunkCache;

    public NoiseResource() {
        this(NoiseTileSize.DEFAULT);
    }

    public NoiseResource(NoiseTileSize tileSize) {
        this.heightmap = new float[tileSize.regionSize];
        this.sharedSample = new NoiseSample();
        this.chunkSample = new ObjectMap<>(1, NoiseSample[]::new);
        this.chunkSample.fill(NoiseSample::new);
        //noinspection unchecked
        this.chunkCache = new CompletableFuture[tileSize.chunkSize];
    }

    public NoiseSample getSample(int dx, int dz) {
        return NoiseData.isInsideChunk(dx, dz) ? chunkSample.get(dx, dz) : sharedSample;
    }
}
