package com.terraforged.core.world;

import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.Seed;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.core.world.terrain.Terrains;
import com.terraforged.core.world.terrain.provider.StandardTerrainProvider;
import com.terraforged.core.world.terrain.provider.TerrainProviderFactory;

public class GeneratorContext {

    public final Seed seed;
    public final Levels levels;
    public final Terrains terrain;
    public final Settings settings;
    public final TerrainProviderFactory terrainFactory;

    public GeneratorContext(Terrains terrain, Settings settings) {
        this(terrain, settings, StandardTerrainProvider::new);
    }

    public GeneratorContext(Terrains terrain, Settings settings, TerrainProviderFactory terrainFactory) {
        this.terrain = terrain;
        this.settings = settings;
        this.seed = new Seed(settings.generator.seed);
        this.levels = new Levels(settings.generator);
        this.terrainFactory = terrainFactory;
    }

    private GeneratorContext(GeneratorContext src) {
        seed = new Seed(src.seed.get());
        levels = src.levels;
        terrain = src.terrain;
        settings = src.settings;
        terrainFactory = src.terrainFactory;
    }

    public GeneratorContext copy() {
        return new GeneratorContext(this);
    }
}
