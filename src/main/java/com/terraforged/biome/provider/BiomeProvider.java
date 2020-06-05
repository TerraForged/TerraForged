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

package com.terraforged.biome.provider;

import com.google.common.collect.Sets;
import com.terraforged.biome.map.BiomeMap;
import com.terraforged.biome.modifier.BiomeModifierManager;
import com.terraforged.chunk.TerraContext;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.util.setup.SetupHooks;
import com.terraforged.world.heightmap.WorldLookup;
import com.terraforged.world.terrain.decorator.Decorator;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class BiomeProvider extends AbstractBiomeProvider {

    private final BiomeMap biomeMap;
    private final BiomeAccess biomeAccess;
    private final TerraContext context;
    private final WorldLookup worldLookup;
    private final BiomeModifierManager modifierManager;
    private final Map<Biome, List<Decorator>> decorators = new HashMap<>();

    public BiomeProvider(TerraContext context) {
        this.context = context;
        this.biomeMap = BiomeHelper.getDefaultBiomeMap();
        this.biomeAccess = new BiomeAccess(context.levels);
        this.worldLookup = new WorldLookup(context.factory, context);
        this.modifierManager = SetupHooks.setup(new BiomeModifierManager(context, biomeMap), context.copy());
    }

    public Resource<Cell> lookupPos(int x, int z) {
        return getWorldLookup().getCell(x, z);
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
    public Set<Biome> getBiomesInSquare(int centerX, int centerY, int centerZ, int radius) {
        int minX = centerX - radius >> 2;
        int minZ = centerZ - radius >> 2;
        int maxX = centerX + radius >> 2;
        int maxZ = centerZ + radius >> 2;
        int rangeX = maxX - minX + 1;
        int rangeZ = maxZ - minZ + 1;
        Set<Biome> set = Sets.newHashSet();
        Cell cell = new Cell();
        for(int dz = 0; dz < rangeZ; ++dz) {
            for(int dx = 0; dx < rangeX; ++dx) {
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
    public BlockPos findBiomePosition(int centerX, int centerY, int centerZ, int range, List<Biome> biomes, Random random) {
        int minX = centerX - range >> 2;
        int minZ = centerZ - range >> 2;
        int maxX = centerX + range >> 2;
        int maxZ = centerZ + range >> 2;
        int rangeX = maxX - minX + 1;
        int rangeZ = maxZ - minZ + 1;
        int y = centerY >> 2;
        BlockPos blockpos = null;
        int attempts = 0;

        Cell cell = new Cell();
        for(int dz = 0; dz < rangeZ; ++dz) {
            for(int dx = 0; dx < rangeX; ++dx) {
                int x = (minX + dx) << 2;
                int z = (minZ + dz) << 2;
                worldLookup.applyCell(cell, x, z);
                if (biomes.contains(getBiome(cell, x, z))) {
                    if (blockpos == null || random.nextInt(attempts + 1) == 0) {
                        blockpos = new BlockPos(x, y, z);
                    }
                    ++attempts;
                }
            }
        }
        return blockpos;
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

    public List<Decorator> getDecorators(Biome biome) {
        return decorators.getOrDefault(biome, Collections.emptyList());
    }

    public Biome getBiome(Cell cell, int x, int z) {
        BiomeAccessor accessor = biomeAccess.getAccessor(cell);
        Biome biome = accessor.getBiome(biomeMap, cell);
        if (modifierManager.hasModifiers(cell.terrain)) {
            return modifierManager.modify(biome, cell, x, z);
        }
        return biome;
    }

    public boolean canSpawnAt(Cell cell) {
        return cell.terrain != context.terrain.ocean && cell.terrain != context.terrain.deepOcean;
    }
}
