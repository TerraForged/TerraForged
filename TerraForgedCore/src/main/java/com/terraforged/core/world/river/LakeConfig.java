package com.terraforged.core.world.river;

import com.terraforged.core.settings.RiverSettings;
import com.terraforged.core.world.heightmap.Levels;

public class LakeConfig {

    public final float depth;
    public final float chance;
    public final float sizeMin;
    public final float sizeMax;
    public final float bankMin;
    public final float bankMax;
    public final float distanceMin;
    public final float distanceMax;

    private LakeConfig(Builder builder) {
        depth = builder.depth;
        chance = builder.chance;
        sizeMin = builder.sizeMin;
        sizeMax = builder.sizeMax;
        bankMin = builder.bankMin;
        bankMax = builder.bankMax;
        distanceMin = builder.distanceMin;
        distanceMax = builder.distanceMax;
    }

    public static LakeConfig of(RiverSettings.Lake settings, Levels levels) {
        Builder builder = new Builder();
        builder.chance = settings.chance;
        builder.sizeMin = settings.sizeMin;
        builder.sizeMax = settings.sizeMax;
        builder.depth = levels.water(-settings.depth);
        builder.distanceMin = settings.minStartDistance;
        builder.distanceMax = settings.maxStartDistance;
        builder.bankMin = levels.water(settings.minBankHeight);
        builder.bankMax = levels.water(settings.maxBankHeight);
        return new LakeConfig(builder);
    }

    public static class Builder {
        public float chance;
        public float depth = 10;
        public float sizeMin = 30;
        public float sizeMax = 100;
        public float bankMin = 1;
        public float bankMax = 8;
        public float distanceMin = 0.025F;
        public float distanceMax = 0.05F;
    }
}
