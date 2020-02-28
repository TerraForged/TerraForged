/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.chunk.fix;

import com.terraforged.feature.util.WorldDelegate;
import com.terraforged.mod.chunk.TerraContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;

public class RegionFix extends WorldDelegate {

    private final WorldGenRegion region;
    private final TerraContainer container;
    private final BiomeManager biomeManager;
    private final ChunkGenerator<?> generator;

    public RegionFix(WorldGenRegion region, TerraContainer container, ChunkGenerator<?> generator, BiomeManager biomeManager) {
        super(region);
        this.region = region;
        this.container = container;
        this.generator = generator;
        this.biomeManager = biomeManager;
    }

    @Override
    public int getSeaLevel() {
        return generator.getSeaLevel();
    }

    @Override
    public int getMaxHeight() {
        return generator.getMaxHeight();
    }

    @Override
    public BiomeManager func_225523_d_() {
        return biomeManager;
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        return getBiome(x, y, z);
    }

    @Override
    public Biome getNoiseBiomeRaw(int x, int y, int z) {
        return getBiome(x, y, z);
    }

    @Override
    public Biome getBiome(BlockPos pos) {
        return getBiome(pos.getX(), pos.getY(), pos.getZ());
    }

    private Biome getBiome(int x, int y, int z) {
        int chunkX = x >> 4;
        int chunkZ = z >> 4;
        if (chunkX == region.getMainChunkX() && chunkZ == region.getMainChunkZ()) {
            return container.getBiome(x, z);
        }

        TerraContainer container = getBiomes(chunkX, chunkZ);
        if (container == null) {
            return generator.getBiomeProvider().getNoiseBiome(x, y, z);
        }

        return container.getBiome(x, z);
    }

    private TerraContainer getBiomes(int chunkX, int chunkZ) {
        IChunk chunk = getChunk(chunkX, chunkZ, ChunkStatus.BIOMES, false);
        if (chunk != null) {
            BiomeContainer container = chunk.getBiomes();
            if (container instanceof TerraContainer) {
                return (TerraContainer) container;
            }
        }
        return null;
    }
}
