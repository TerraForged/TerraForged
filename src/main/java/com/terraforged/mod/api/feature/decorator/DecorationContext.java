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

package com.terraforged.mod.api.feature.decorator;

import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionDelegate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;

public class DecorationContext {

    private final IChunk chunk;
    private final Levels levels;
    private final WorldGenRegion region;
    private final TFBiomeContainer biomes;
    private final TFChunkGenerator generator;

    public DecorationContext(WorldGenRegion region, IChunk chunk, TFBiomeContainer biomes, TFChunkGenerator generator) {
        this.chunk = chunk;
        this.region = region;
        this.biomes = biomes;
        this.generator = generator;
        this.levels = generator.getContext().levels;
    }

    public IChunk getChunk() {
        return chunk;
    }

    public WorldGenRegion getRegion() {
        return region;
    }

    public TFBiomeContainer getBiomes() {
        return biomes;
    }

    public Biome getBiome(BlockPos pos) {
        return region.getBiome(pos);
    }

    public Levels getLevels() {
        return levels;
    }

    public TFChunkGenerator getGenerator() {
        return generator;
    }

    public static DecorationContext of(ISeedReader world, ChunkGenerator generator) {
        if (generator instanceof TFChunkGenerator && world instanceof RegionDelegate) {
            TFChunkGenerator terraGenerator = (TFChunkGenerator) generator;
            WorldGenRegion region = ((RegionDelegate) world).getDelegate();
            IChunk chunk = region.getChunk(region.getCenterX(), region.getCenterZ());
            if (chunk.getBiomes() instanceof TFBiomeContainer) {
                TFBiomeContainer container = (TFBiomeContainer) chunk.getBiomes();
                return new DecorationContext(region, chunk, container, terraGenerator);
            }
        }
        return null;
    }
}
