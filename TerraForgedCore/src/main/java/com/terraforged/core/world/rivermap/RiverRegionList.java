package com.terraforged.core.world.rivermap;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.concurrent.cache.CacheEntry;
import com.terraforged.core.world.rivermap.river.RiverRegion;
import com.terraforged.core.world.terrain.Terrain;

public class RiverRegionList {

    private int index = 0;
    private final CacheEntry<RiverRegion>[] regions = new CacheEntry[4];

    protected void add(CacheEntry<RiverRegion> entry) {
        if (index < regions.length) {
            regions[index] = entry;
            index++;
        }
    }

    public void apply(Cell<Terrain> cell, float x, float z) {
        int complete = 0;
        while (complete < regions.length) {
            for (CacheEntry<RiverRegion> entry : regions) {
                if (entry.isDone()) {
                    complete++;
                    entry.get().apply(cell, x, z);
                }
            }
        }
    }
}
