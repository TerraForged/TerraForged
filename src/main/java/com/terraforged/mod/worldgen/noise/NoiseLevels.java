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

import com.terraforged.mod.worldgen.terrain.TerrainLevels;

public class NoiseLevels {
    public final float depthMin;
    public final float depthRange;

    public final float heightMin;
    public final float heightRange;

    public final float frequency;

    public NoiseLevels(int seaLevel, int genDepth) {
        this.depthMin = (seaLevel - 40) / (float) genDepth;
        this.heightMin = seaLevel / (float) genDepth;
        this.heightRange = 1F - heightMin;
        this.depthRange = heightMin - depthMin;
        this.frequency = calcFrequency(genDepth - seaLevel);
    }

    public float toDepthNoise(float noise) {
        return depthMin + noise * depthRange;
    }

    public float toHeightNoise(float noise) {
        return heightMin + noise * heightRange;
    }

    public static NoiseLevels getDefault() {
        return TerrainLevels.DEFAULT.noiseLevels;
    }

    public static float calcFrequency(int verticalRange) {
        return (TerrainLevels.DEFAULT_GEN_DEPTH - TerrainLevels.DEFAULT_SEA_LEVEL) / (float) verticalRange;
    }
}
