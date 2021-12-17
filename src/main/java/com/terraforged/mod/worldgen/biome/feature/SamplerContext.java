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

package com.terraforged.mod.worldgen.biome.feature;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.VegetationConfig;
import com.terraforged.mod.worldgen.biome.util.BiomeList;
import com.terraforged.mod.worldgen.biome.vegetation.BiomeVegetation;
import com.terraforged.mod.worldgen.biome.vegetation.VegetationFeatures;
import com.terraforged.mod.worldgen.biome.viability.ViabilityContext;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class SamplerContext {
    private static final ThreadLocal<SamplerContext> LOCAL_CONTEXT = ThreadLocal.withInitial(SamplerContext::new);

    public ChunkAccess chunk;
    public WorldGenLevel region;
    public Generator generator;
    public WorldgenRandom random;

    public Biome biome;
    public VegetationConfig vegetation;
    public VegetationFeatures features;
    public float maxViability = 0F;

    public final BiomeList biomeList = new BiomeList();
    public final ViabilityContext viabilityContext = new ViabilityContext();
    public final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

    public int getHeight(int x, int z) {
        return chunk.getHeight(Heightmap.Types.OCEAN_FLOOR_WG, x, z);
    }

    public Biome getBiome(int x, int y, int z) {
        return region.getBiome(pos.set(x, y, z));
    }

    public TerrainData terrainData() {
        return viabilityContext.getTerrain();
    }

    public SamplerContext reset() {
        biomeList.reset();
        return this;
    }

    public void push(Biome biome, BiomeVegetation vegetation) {
        this.maxViability = 0F;
        this.biome = biome;
        this.vegetation = vegetation.config;
        this.features = vegetation.features;
    }

    public static SamplerContext get() {
        return LOCAL_CONTEXT.get().reset();
    }
}
