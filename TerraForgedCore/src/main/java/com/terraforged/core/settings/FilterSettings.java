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

        @Range(min = 0, max = 30000)
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
