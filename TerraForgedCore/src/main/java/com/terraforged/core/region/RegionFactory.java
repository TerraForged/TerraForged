package com.terraforged.core.region;

public interface RegionFactory {

    Region create(int regionX, int regionZ, int size, int borderChunks);
}
