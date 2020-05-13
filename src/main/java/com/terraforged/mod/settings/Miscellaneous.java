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

package com.terraforged.mod.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Serializable;

@Serializable
public class Miscellaneous {

    @Comment("Modifies layer block levels (ie snow) to fit the terrain")
    public boolean smoothLayerDecorator = true;

    @Comment("Generates strata (rock layers) instead of just stone")
    public boolean strataDecorator = true;

    @Comment("Replace surface materials where erosion has occurred")
    public boolean erosionDecorator = true;

    @Comment("Removes snow from the terrain where it shouldn't naturally settle")
    public boolean naturalSnowDecorator = true;

    @Comment("Use custom biome features in place of vanilla ones (such as trees)")
    public boolean customBiomeFeatures = true;

    @Comment("Controls whether vanilla lakes & springs should generate")
    public boolean vanillaWaterFeatures = false;
}
