package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;

@Serializable
public class TerrainSettings {

    public transient float deepOcean = 1F;
    public transient float ocean = 1F;
    public transient float coast = 1F;
    public transient float river = 1F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float steppe = 5F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float plains = 5F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float hills = 2F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float dales = 2F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float plateau = 2F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float badlands = 2F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float torridonian = 0.5F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float mountains = 0.5F;

    @Range(min = 0, max = 10)
    @Comment("Controls how common this terrain type is")
    public float volcano = 1F;
}
