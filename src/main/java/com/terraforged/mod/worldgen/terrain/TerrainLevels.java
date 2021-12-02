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

import com.terraforged.mod.worldgen.noise.NoiseLevels;
import com.terraforged.noise.util.NoiseUtil;

public class TerrainLevels {
    public static final int DEFAULT_SEA_LEVEL = 63;
    public static final int DEFAULT_GEN_DEPTH = 255;
    public static final TerrainLevels DEFAULT = new TerrainLevels(DEFAULT_SEA_LEVEL, DEFAULT_GEN_DEPTH);

    public final int seaLevel;
    public final int genDepth;
    public final NoiseLevels noiseLevels;

    public TerrainLevels() {
        this(DEFAULT_SEA_LEVEL, 384);
    }

    public TerrainLevels(int seaLevel, int genDepth) {
        this.seaLevel = seaLevel;
        this.genDepth = genDepth;
        this.noiseLevels = new NoiseLevels(seaLevel, genDepth);
    }

    public float getScaledHeight(float heightNoise) {
        return heightNoise * genDepth;
    }

    public int getHeight(float scaledHeight) {
        return NoiseUtil.floor(scaledHeight);
    }
}
