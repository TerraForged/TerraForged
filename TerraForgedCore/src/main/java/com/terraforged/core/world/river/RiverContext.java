package com.terraforged.core.world.river;

public class RiverContext {

    public final float frequency;
    public final RiverConfig primary;
    public final RiverConfig secondary;
    public final RiverConfig tertiary;
    public final LakeConfig lakes;

    public RiverContext(float frequency, RiverConfig primary, RiverConfig secondary, RiverConfig tertiary, LakeConfig lakes) {
        this.frequency = frequency;
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.lakes = lakes;
    }
}
