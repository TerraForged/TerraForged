package com.terraforged.core.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.concurrent.cache.CacheEntry;
import com.terraforged.core.world.rivermap.river.RiverRegion;
import com.terraforged.core.world.terrain.Terrain;

import java.util.Arrays;

public class RiverRegionList {

    private int index = 0;
    private final CacheEntry<RiverRegion>[] regions = new CacheEntry[4];

    protected void add(CacheEntry<RiverRegion> entry) {
        if (index < regions.length) {
            regions[index] = entry;
            index++;
        }
    }

    protected void reset() {
        index = 0;
    }

    public void clear() {
        Arrays.fill(regions, null);
    }

    public void apply(Cell<Terrain> cell, float x, float z) {
        for (CacheEntry<RiverRegion> entry : regions) {
            entry.get().apply(cell, x, z);
        }
    }
}
