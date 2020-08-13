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

package com.terraforged.mod.biome.provider;

import com.google.common.collect.Sets;
import com.mojang.serialization.Codec;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.modifier.BiomeModifierManager;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.util.setup.SetupHooks;
import com.terraforged.world.heightmap.WorldLookup;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.ColumnFuzzedBiomeMagnifier;
import net.minecraft.world.biome.provider.BiomeProvider;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.Set;
import java.util.function.Predicate;

public class TerraBiomeProvider extends BiomeProvider {

    private final long seed;
    private final BiomeMap biomeMap;
    private final TerraContext context;
    private final WorldLookup worldLookup;
    private final BiomeModifierManager modifierManager;

    public TerraBiomeProvider(TerraContext context) {
        super(BiomeHelper.getAllBiomes(context.gameContext));
        this.context = context;
        this.seed = context.terraSettings.world.seed;
        this.biomeMap = BiomeHelper.createBiomeMap(context.gameContext);
        this.worldLookup = new WorldLookup(context.factory, context);
        this.modifierManager = SetupHooks.setup(new BiomeModifierManager(context, biomeMap), context.copy());
    }

    public Resource<Cell> lookupPos(int x, int z) {
        return getWorldLookup().getCell(x, z);
    }

    public Biome getBiome(int x, int z) {
        try (Resource<Cell> resource = getWorldLookup().getCell(x, z, true)) {
            return getBiome(resource.get(), x, z);
        }
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z) {
        x = (x << 2);
        z = (z << 2);
        try (Resource<Cell> cell = lookupPos(x, z)) {
            return getBiome(cell.get(), x, z);
        }
    }

    @Override
    protected Codec<? extends BiomeProvider> func_230319_a_() {
        return null;
    }

    @Override
    public BiomeProvider func_230320_a_(long seed) {
        return this;
    }

    @Override
    public Set<Biome> getBiomes(int centerX, int centerY, int centerZ, int radius) {
        int minX = centerX - radius >> 2;
        int minZ = centerZ - radius >> 2;
        int maxX = centerX + radius >> 2;
        int maxZ = centerZ + radius >> 2;
        int rangeX = maxX - minX + 1;
        int rangeZ = maxZ - minZ + 1;
        Set<Biome> set = Sets.newHashSet();
        Cell cell = new Cell();
        for (int dz = 0; dz < rangeZ; ++dz) {
            for (int dx = 0; dx < rangeX; ++dx) {
                int x = (minX + dx) << 2;
                int z = (minZ + dz) << 2;
                worldLookup.applyCell(cell, x, z);
                Biome biome = getBiome(cell, x, z);
                set.add(biome);
            }
        }

        return set;
    }

    @Override
    @Nullable
    public BlockPos func_230321_a_(int centerX, int centerY, int centerZ, int radius, int increment, Predicate<Biome> biomes, Random random, boolean centerOutSearch) {
        // convert block coords to biome coords
        int biomeRadius = radius >> 2;
        int biomeCenterX = centerX >> 2;
        int biomeCenterZ = centerZ >> 2;

        Cell cell = new Cell();
        BlockPos.Mutable pos = null;

        // keeps track of the number of matched positions and progressively reduces the likelihood of a matching position
        // being selected as the result
        int count = 0;

        // centerOutSearch iterates concentric rings around the center coordinates to find the closest matching position
        // non-centerOut iterates the entire square around the center and returns a random matching position
        int startRadius = centerOutSearch ? 0 : biomeRadius;

        for (int r = startRadius; r < biomeRadius; r += increment) {
            for (int dz = -r; dz <= r; dz++) {
                boolean onRadiusZ = Math.abs(dz) == r;

                for (int dx = -r; dx <= r; dx++) {
                    if (centerOutSearch) {
                        boolean onRadiusX = Math.abs(dx) == r;
                        if (!onRadiusX && !onRadiusZ) {
                            continue;
                        }
                    }

                    int biomeX = biomeCenterX + dx;
                    int biomeZ = biomeCenterZ + dz;

                    // getBiome(Cell,int,int) expects block coords, not biome coords
                    int x = biomeX << 2;
                    int z = biomeZ << 2;

                    if (biomes.test(getBiome(cell, x, z))) {
                        if (centerOutSearch) {
                            return new BlockPos(x, centerY, z);
                        }

                        if (pos == null) {
                            pos = new BlockPos.Mutable(x, centerY, z);
                        } else if (random.nextInt(count + 1) == 0) {
                            // as the match count increases the chance of getting a zero reduces
                            pos.setPos(x, centerY, z);
                        }

                        count++;
                    }
                }
            }
        }
        return pos;
    }

    public Biome getSurfaceBiome(int x, int z, BiomeManager.IBiomeReader reader) {
        return ColumnFuzzedBiomeMagnifier.INSTANCE.getBiome(seed, x, 0, z, reader);
    }

    public WorldLookup getWorldLookup() {
        return worldLookup;
    }

    public TerraContext getContext() {
        return context;
    }

    public BiomeModifierManager getModifierManager() {
        return modifierManager;
    }

    public Biome getBiome(Cell cell, int x, int z) {
        Biome biome = biomeMap.provideBiome(cell, context.levels);
        if (modifierManager.hasModifiers(cell, context.levels)) {
            return modifierManager.modify(biome, cell, x, z);
        }
        return biome;
    }

    public boolean canSpawnAt(Cell cell) {
        return cell.terrain != context.terrain.ocean && cell.terrain != context.terrain.deepOcean;
    }
}
