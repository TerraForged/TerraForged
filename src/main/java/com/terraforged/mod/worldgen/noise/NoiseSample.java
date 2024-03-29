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
import com.terraforged.mod.worldgen.asset.TerrainType;

public class NoiseSample {
    private static final NoiseSample DEFAULT = new NoiseSample();

    public long continentCentre = 0;
    public float continentNoise = 1;
    public float baseNoise = 0;
    public float heightNoise = 0;
    public float riverNoise = 1;
    public Terrain terrainType = TerrainType.NONE.getTerrain();

    public NoiseSample() {}

    public NoiseSample(NoiseSample other) {
        copy(other);
    }

    public NoiseSample reset() {
        return copy(DEFAULT);
    }

    public NoiseSample copy(NoiseSample other) {
        continentCentre = other.continentCentre;
        continentNoise = other.continentNoise;
        heightNoise = other.heightNoise;
        baseNoise = other.baseNoise;
        riverNoise = other.riverNoise;
        terrainType = other.terrainType;
        return this;
    }
}
