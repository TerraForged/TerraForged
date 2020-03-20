package com.terraforged.core.world.rivermap;

import com.terraforged.core.world.rivermap.lake.LakeConfig;
import com.terraforged.core.world.rivermap.river.RiverConfig;

public class RiverMapConfig {

    public final float frequency;
    public final RiverConfig primary;
    public final RiverConfig secondary;
    public final RiverConfig tertiary;
    public final LakeConfig lakes;

    public RiverMapConfig(float frequency, RiverConfig primary, RiverConfig secondary, RiverConfig tertiary, LakeConfig lakes) {
        this.frequency = frequency;
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.lakes = lakes;
    }
}
