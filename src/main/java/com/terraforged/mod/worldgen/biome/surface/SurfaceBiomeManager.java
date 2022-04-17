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

package com.terraforged.mod.worldgen.biome.surface;

import com.terraforged.mod.worldgen.util.delegate.DelegateBiomeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;

public class SurfaceBiomeManager extends DelegateBiomeManager {
    private static final ThreadLocal<SurfaceBiomeManager> LOCAL_SURFACE_MANAGER = ThreadLocal.withInitial(SurfaceBiomeManager::new);

    protected ChunkPos chunkPos;
    protected final Holder<Biome>[] surfaceCache = new Holder[16 * 16];
    protected final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    SurfaceBiomeManager set(ChunkPos chunkPos, BiomeManager biomeManager) {
        super.set(biomeManager);
        this.chunkPos = chunkPos;

        int startX = chunkPos.getMinBlockX();
        int startZ = chunkPos.getMinBlockZ();
        for (int dz = 0; dz < 16; dz++) {
            for (int dx = 0; dx < 16; dx++) {
                pos.set(startX + dx, 0, startZ + dz);
                surfaceCache[index(dx, dz)] = biomeManager.getBiome(pos);;
            }
        }
        return this;
    }

    @Override
    public Holder<Biome> getBiome(BlockPos pos) {
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;

        if (chunkX == chunkPos.x && chunkZ == chunkPos.z) {
            int dx = pos.getX() - (chunkX << 4);
            int dz = pos.getZ() - (chunkZ << 4);
            return surfaceCache[index(dx, dz)];
        }

        return super.getBiome(pos);
    }

    protected static int index(int x, int z) {
        return (z << 4) | x;
    }

    public static BiomeManager assign(ChunkPos chunkPos, BiomeManager biomeManager) {
        return LOCAL_SURFACE_MANAGER.get().set(chunkPos, biomeManager);
    }
}
