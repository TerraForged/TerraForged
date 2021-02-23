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

package com.terraforged.mod.api.material.geology;

import com.terraforged.noise.util.NoiseUtil;

public class StrataConfig {

    public Config soil = new Config(0, 1, 0.1F, 0.25F);
    public Config sediment = new Config(0, 2, 0.05F, 0.15F);
    public Config clay = new Config(0, 2, 0.05F, 0.1F);
    public Config rock = new Config(10, 30, 0.1F, 1.5F);

    public static class Config {

        public int minLayers;
        public int maxLayers;
        public float minDepth;
        public float maxDepth;

        public Config() {
        }

        public Config(int minLayers, int maxLayers, float minDepth, float maxDepth) {
            this.minLayers = minLayers;
            this.maxLayers = maxLayers;
            this.minDepth = minDepth;
            this.maxDepth = maxDepth;
        }

        public int getLayers(float value) {
            int range = maxLayers - minLayers;
            return minLayers + NoiseUtil.round(value * range);
        }

        public float getDepth(float value) {
            float range = maxDepth - minDepth;
            return minDepth + value * range;
        }
    }
}
