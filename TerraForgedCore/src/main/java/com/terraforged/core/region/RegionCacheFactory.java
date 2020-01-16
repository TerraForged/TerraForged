package com.terraforged.core.region;

import com.terraforged.core.world.WorldGeneratorFactory;

public interface RegionCacheFactory {

    RegionCache create(WorldGeneratorFactory factory);
}
