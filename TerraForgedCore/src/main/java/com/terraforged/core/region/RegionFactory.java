package com.terraforged.core.region;

import com.terraforged.core.util.concurrent.Disposable;

public interface RegionFactory {

    Region create(int regionX, int regionZ, int size, int borderChunks, Disposable.Listener<Region> listener);
}
