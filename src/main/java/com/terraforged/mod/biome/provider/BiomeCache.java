/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.biome.provider;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.util.pos.PosUtil;
import com.terraforged.engine.world.biome.map.BiomeMap;
import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.HashCommon;
import it.unimi.dsi.fastutil.longs.Long2IntLinkedOpenHashMap;

import java.util.concurrent.locks.StampedLock;

public class BiomeCache {

    private static final int CACHE_SIZE = 256 * 256;

    private final int maxSize;
    private final TFBiomeProvider biomeProvider;
    private final Long2IntLinkedOpenHashMap cache;
    private final StampedLock lock = new StampedLock();

    public BiomeCache(TFBiomeProvider biomeProvider) {
        this.biomeProvider = biomeProvider;
        this.cache = new Long2IntLinkedOpenHashMap(CACHE_SIZE, Hash.DEFAULT_LOAD_FACTOR);
        this.cache.defaultReturnValue(BiomeMap.NULL_BIOME);
        this.maxSize = HashCommon.maxFill(CACHE_SIZE, Hash.DEFAULT_LOAD_FACTOR) - 2;
    }

    public int tryGetBiome(Cell cell, int blockX, int blockZ, boolean load) {
        if (isCacheable(blockX, blockZ)) {
            final int biomeX = blockX >> 2;
            final int biomeZ = blockZ >> 2;
            return getNoiseBiome(cell, biomeX, biomeZ, load);
        }
        return BiomeMap.NULL_BIOME;
    }

    public int getNoiseBiome(Cell cell, int biomeX, int biomeZ, boolean load) {
        final long key = PosUtil.pack(biomeX, biomeZ);

        final long read = lock.tryReadLock();
        if (read == 0L) {
            return computeValue(cell, biomeX, biomeZ, load);
        }

        try {
            int value = cache.get(key);
            if (value != BiomeMap.NULL_BIOME) {
                return value;
            }

            final long write = lock.tryConvertToWriteLock(read);
            if (write == 0L) {
                return computeValue(cell, biomeX, biomeZ, load);
            }

            try {
                value = computeValue(cell, biomeX, biomeZ, load);
                storeValue(key, value);
                return value;
            } finally {
                lock.unlockWrite(write);
            }
        } finally {
            if (lock.validate(read)) {
                lock.unlockRead(read);
            }
        }
    }

    public void tryStoreBiome(int blockX, int blockZ, int value) {
        if (isCacheable(blockX, blockZ)) {
            final int biomeX = blockX >> 2;
            final int biomeZ = blockZ >> 2;
            tryStoreNoiseBiome(biomeX, biomeZ, value);
        }
    }

    public void tryStoreNoiseBiome(int biomeX, int biomeZ, int value) {
        final long key = PosUtil.pack(biomeX, biomeZ);
        final long write = lock.tryWriteLock();
        if (write != 0) {
            try {
                storeValue(key, value);
            } finally {
                lock.unlockWrite(write);
            }
        }
    }

    private int computeValue(Cell cell, int biomeX, int biomeZ, boolean load) {
        int blockX = biomeX << 2;
        int blockZ = biomeZ << 2;
        return biomeProvider.computeBiome(cell, blockX, blockZ, load);
    }

    private void storeValue(long key, int value) {
        if (cache.size() >= maxSize) {
            cache.removeFirstInt();
        }
        cache.put(key, value);
    }

    private static boolean isCacheable(int blockX, int blockZ) {
        return (blockX & 3) == 0 && (blockZ & 3) == 0;
    }
}
