package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Serializable;

@Serializable
public class Settings {

    public GeneratorSettings generator = new GeneratorSettings();

    public RiverSettings rivers = new RiverSettings();

    public FilterSettings filters = new FilterSettings();

    public TerrainSettings terrain = new TerrainSettings();

    public BiomeSettings biomes = new BiomeSettings();
}
