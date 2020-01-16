package com.terraforged.core.world.terrain.provider;

import com.terraforged.core.cell.Populator;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.RegionConfig;

public interface TerrainProviderFactory {

    TerrainProvider create(GeneratorContext context, RegionConfig config, Populator defaultPopulator);
}
