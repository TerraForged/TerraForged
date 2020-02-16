package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;

@Serializable
public class RiverSettings {

    /**
     * RIVER PROPERTIES
     */
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
