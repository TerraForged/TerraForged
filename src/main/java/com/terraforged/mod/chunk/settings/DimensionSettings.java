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

package com.terraforged.mod.chunk.settings;

import com.terraforged.engine.serialization.annotation.Comment;
import com.terraforged.engine.serialization.annotation.Range;
import com.terraforged.engine.serialization.annotation.Serializable;

@Serializable
public class DimensionSettings {

    public BaseDecorator bedrockLayer = new BaseDecorator();

    public DimensionGenerators dimensions = new DimensionGenerators();

    @Serializable
    public static class BaseDecorator {

        @Comment("Controls the material that should be used in the world's base layer")
        public String material = "minecraft:bedrock";

        @Range(min = 0, max = 10)
        @Comment("Controls the minimum height of the world's base layer")
        public int minDepth = 1;

        @Range(min = 0, max = 10)
        @Comment("Controls the amount of height randomness of the world's base layer")
        public int variance = 4;
    }

    @Serializable
    public static class DimensionGenerators {

        @Comment("Select the nether generator")
        public String nether = "default";

        @Comment("Select the end generator")
        public String end = "default";

        @Comment("Find extra mod-provided dimensions to generate (ie non overworld/nether/end dimensions)")
        public boolean includeExtraDimensions = true;
    }
}
