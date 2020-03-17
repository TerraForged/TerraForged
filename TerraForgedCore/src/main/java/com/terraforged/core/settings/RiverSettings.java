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
public class RiverSettings {

    /**
     * RIVER PROPERTIES
     */
    @Range(min = 0.0F, max = 5F)
    @Comment("Controls how frequently rivers generate")
    public float riverFrequency = 1;

    public River primaryRivers = new River(5, 2, 8, 25, 8, 0.75F);

    public River secondaryRiver = new River(4, 1, 6, 15, 5, 0.75F);

    public River tertiaryRivers = new River(3, 0, 4, 10, 4, 0.75F);

    public Lake lake = new Lake();

    @Serializable
    public static class River {

        @Range(min = 1, max = 10)
        @Comment("Controls the depth of the river")
        public int bedDepth;

        @Range(min = 1, max = 10)
        @Comment("Controls the height of river banks")
        public int minBankHeight;

        @Range(min = 1, max = 10)
        @Comment("Controls the height of river banks")
        public int maxBankHeight;

        @Range(min = 1, max = 20)
        @Comment("Controls the river-bed width")
        public int bedWidth;

        @Range(min = 1, max = 50)
        @Comment("Controls the river-banks width")
        public int bankWidth;

        @Range(min = 0.0F, max = 1.0F)
        @Comment("Controls how much rivers taper")
        public float fade;

        public River() {
        }

        public River(int depth, int minBank, int maxBank, int outer, int inner, float fade) {
            this.minBankHeight = minBank;
            this.maxBankHeight = maxBank;
            this.bankWidth = outer;
            this.bedWidth = inner;
            this.bedDepth = depth;
            this.fade = fade;
        }
    }

    public static class Lake {

        @Range(min = 0.0F, max = 1.0F)
        @Comment("Controls the chance of a lake spawning")
        public float chance = 0.2F;

        @Range(min = 0F, max = 1F)
        @Comment("The minimum distance along a river that a lake will spawn")
        public float minStartDistance = 0.03F;

        @Range(min = 0F, max = 1F)
        @Comment("The maximum distance along a river that a lake will spawn")
        public float maxStartDistance = 0.07F;

        @Range(min = 1, max = 20)
        @Comment("The max depth of the lake")
        public int depth = 10;

        @Range(min = 10, max = 50)
        @Comment("The minimum size of the lake")
        public int sizeMin = 50;

        @Range(min = 50, max = 150)
        @Comment("The maximum size of the lake")
        public int sizeMax = 100;

        @Range(min = 1, max = 10)
        @Comment("The minimum bank height")
        public int minBankHeight = 2;

        @Range(min = 1, max = 10)
        @Comment("The maximum bank height")
        public int maxBankHeight = 10;

        public Lake() {

        }
    }
}
