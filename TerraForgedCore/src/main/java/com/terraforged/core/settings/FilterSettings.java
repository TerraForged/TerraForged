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

@Serializable
public class FilterSettings {

    public Erosion erosion = new Erosion();

    public Smoothing smoothing = new Smoothing();

    @Serializable
    public static class Erosion {

        @Range(min = 1000, max = 30000)
        @Comment("Controls the number of erosion iterations")
        public int iterations = 15000;

        @Range(min = 0F, max = 1F)
        @Comment("Controls how quickly material dissolves (during erosion)")
        public float erosionRate = 0.35F;

        @Range(min = 0F, max = 1F)
        @Comment("Controls how quickly material is deposited (during erosion)")
        public float depositeRate = 0.5F;
    }

    @Serializable
    public static class Smoothing {

        @Range(min = 0, max = 5)
        @Comment("Controls the number of smoothing iterations")
        public int iterations = 1;

        @Range(min = 0, max = 5)
        @Comment("Controls the smoothing radius")
        public float smoothingRadius = 1.75F;

        @Range(min = 0, max = 1)
        @Comment("Controls how strongly smoothing is applied")
        public float smoothingRate = 0.85F;
    }
}
