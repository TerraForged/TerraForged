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

package com.terraforged.mod.worldgen.biome.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class BufferedBiomeManager extends DelegateBiomeManager {
    private static final ThreadLocal<BufferedBiomeManager> LOCAL_BIOME_MANAGER = ThreadLocal.withInitial(BufferedBiomeManager::new);

    protected int misses;
    protected int requests;
    protected ChunkPos chunkPos;
    protected final Holder<Biome>[] buffer = new Holder[16 * 16];
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    void set(ChunkPos chunkPos, BiomeManager biomeManager) {
        this.misses = 0;
        this.requests = 0;
        this.chunkPos = chunkPos;

        setDelegate(biomeManager);

        int x = chunkPos.getMinBlockX();
        int z = chunkPos.getMinBlockZ();
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                pos.set(x + dx, 64, z + dz);

                var biome = biomeManager.getBiome(pos);
                buffer[index(dx, dz)] = biome;
            }
        }
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        requests++;
        int x = pos.getX() >> 4;
        int z = pos.getZ() >> 4;
        if (x == chunkPos.x && z == chunkPos.z) {
            int dx = pos.getX() - chunkPos.getMinBlockX();
            int dz = pos.getZ() - chunkPos.getMinBlockZ();
            return buffer[index(dx, dz)];
        }
        misses++;
        return delegate.getBiome(pos);
    }

    public void report() {
//        TerraForged.LOG.info("Cache requests: " + requests);
//        TerraForged.LOG.info("Cache misses:   " + misses);
    }

    private static int index(int dx, int dz) {
        return (dz << 4) | dx;
    }

    public static BufferedBiomeManager assign(ChunkPos chunkPos, BiomeManager biomeManager) {
        var manager = LOCAL_BIOME_MANAGER.get();
        manager.set(chunkPos, biomeManager);
        return manager;
    }
}
