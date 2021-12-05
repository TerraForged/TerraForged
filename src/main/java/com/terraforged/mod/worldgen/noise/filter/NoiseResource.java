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

package com.terraforged.mod.worldgen.noise.filter;

import com.terraforged.mod.util.map.ObjectMap;
import com.terraforged.mod.worldgen.noise.NoiseData;
import com.terraforged.mod.worldgen.noise.NoiseSample;
import com.terraforged.mod.worldgen.noise.RiverCache;

public class NoiseResource {
    public static final int CHUNK_SIZE = 16;
    public static final int CHUNK_AREA = CHUNK_SIZE * CHUNK_SIZE;
    public static final int REL_MIN = -CHUNK_SIZE;
    public static final int REL_MAX = CHUNK_SIZE * 2;
    public static final int REGION_SIZE = REL_MAX - REL_MIN;

    public final NoiseData chunk = new NoiseData();
    public final RiverCache riverCache = new RiverCache();

    public final float[] heightmap = new float[CHUNK_AREA * 9];
    public final NoiseSample sharedSample = new NoiseSample();
    public final ObjectMap<NoiseSample> chunkSample = new ObjectMap<>(1, NoiseSample[]::new);

    public NoiseResource() {
        this.chunkSample.fill(NoiseSample::new);
    }

    public NoiseSample getSample(int dx, int dz) {
        return NoiseData.isInsideChunk(dx, dz) ? chunkSample.get(dx, dz) : sharedSample;
    }

    public static int indexOf(int rx, int rz) {
        return rz * REGION_SIZE + rx;
    }
}
