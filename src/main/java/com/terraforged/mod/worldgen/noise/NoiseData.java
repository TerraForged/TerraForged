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

import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.map.FloatMap;
import com.terraforged.mod.util.map.Index;
import com.terraforged.mod.util.map.ObjectMap;

public class NoiseData {
    protected static final int BORDER = 1;
    protected static final int MIN = -BORDER;
    protected static final int MAX = 16 + BORDER;

    protected final NoiseSample sample = new NoiseSample();
    protected final FloatMap height = new FloatMap(BORDER);
    protected final FloatMap water = new FloatMap(BORDER);
    protected final FloatMap moisture = new FloatMap(BORDER);
    protected final FloatMap temperature = new FloatMap(BORDER);
    protected final ObjectMap<Terrain> terrain = new ObjectMap<>(BORDER, Terrain[]::new);

    public int min() {
        return MIN;
    }

    public int max() {
        return MAX;
    }

    public Index index() {
        return height.index();
    }

    public NoiseSample getSample() {
        return sample;
    }

    public FloatMap getHeight() {
        return height;
    }

    public FloatMap getWater() {
        return water;
    }

    public FloatMap getMoisture() {
        return moisture;
    }

    public FloatMap getTemperature() {
        return temperature;
    }

    public ObjectMap<Terrain> getTerrain() {
        return terrain;
    }

    public void setNoise(int x, int z, NoiseSample sample) {
        int index = index().of(x, z);
        terrain.set(index, sample.terrainType);
        height.set(index, sample.heightNoise);
        water.set(index, sample.riverNoise);
    }
}
