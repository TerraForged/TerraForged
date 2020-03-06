/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;
import me.dags.noise.Module;

@Serializable
public class TerrainSettings {

    @Range(min = 0, max = 1)
    @Comment("Globally controls the vertical scaling of terrain")
    public float globalVerticalScale = 0.985F;

    @Range(min = 0, max = 5)
    @Comment("Globally controls the horizontal scaling of terrain")
    public float globalHorizontalScale = 1.0F;

    public Terrain steppe = new Terrain(5F, 1F, 1F);
    public Terrain plains = new Terrain(5F, 1F, 1F);
    public Terrain hills = new Terrain(2F, 1F, 1F);
    public Terrain dales = new Terrain(2F, 1F, 1F);
    public Terrain plateau = new Terrain(2F, 1F, 1F);
    public Terrain badlands = new Terrain(2F, 1F, 1F);
    public Terrain torridonian = new Terrain(0.5F, 1F, 1F);
    public Terrain mountains = new Terrain(0.5F, 1F, 1F);
    public Terrain volcano = new Terrain(1F, 1F, 1F);

    @Serializable
    public static class Terrain {

        @Range(min = 0, max = 10)
        @Comment("Controls how common this terrain type is")
        public float weight = 1F;

        @Range(min = 0, max = 2)
        @Comment("Controls the base height of this terrain")
        public float baseScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain vertically")
        public float verticalScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain horizontally")
        public float horizontalScale = 1F;

        public Terrain() {

        }

        public Terrain(float weight, float vertical, float horizontal) {
            this.weight = weight;
            this.verticalScale = vertical;
            this.horizontalScale = horizontal;
        }

        public Module apply(double bias, double scale, Module module) {
            double moduleBias = bias * baseScale;
            double moduleScale = scale * verticalScale;
            if (moduleBias + moduleScale > 1) {
                return module.scale(moduleScale).bias(moduleBias).clamp(0, 1);
            }
            return module.scale(moduleScale).bias(moduleBias);
        }
    }
}
