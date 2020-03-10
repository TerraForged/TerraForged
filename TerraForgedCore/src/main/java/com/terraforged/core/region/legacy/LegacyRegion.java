package com.terraforged.core.region.legacy;

import com.terraforged.core.region.Region;

/**
 * This is here to provide compatibility for versions 0.0.2 and below which contained a
 * bug where Regions generated with an x,z offset equal to the size of the chunk border
 * around the region (x16) ie 32 blocks
 */
public class LegacyRegion extends Region {

    public LegacyRegion(int regionX, int regionZ, int size, int borderChunks) {
        super(regionX, regionZ, size, borderChunks);
    }

    /**
     * This is used when calculating the 'real world' chunk position of the chunk-view
     * and subsequently the 'real world' block start position. In versions 0.0.2 and
     * below, no offset was being deducted, so return 0 here to maintain that broken
     * behaviour... RIP :/
     */
    @Override
    public int getOffsetChunks() {
        return 0;
    }
}
