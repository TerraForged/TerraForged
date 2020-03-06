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

package com.terraforged.mod.command.task;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.decorator.Decorator;
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.biome.provider.BiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class FindBiomeTask extends FindTask {

    private final Biome biome;
    private final WorldGenerator generator;
    private final BiomeProvider biomeProvider;
    private final Cell<Terrain> cell = new Cell<>();

    public FindBiomeTask(BlockPos center, Biome biome, WorldGenerator generator, BiomeProvider biomeProvider) {
        this(center, biome, generator, biomeProvider, 100);
    }

    public FindBiomeTask(BlockPos center, Biome biome, WorldGenerator generator, BiomeProvider biomeProvider, int minRadius) {
        super(center, minRadius);
        this.biome = biome;
        this.generator = generator;
        this.biomeProvider = biomeProvider;
    }

    @Override
    protected int transformCoord(int coord) {
        return coord * 8;
    }

    @Override
    protected boolean search(int x, int z) {
        generator.getHeightmap().apply(cell, x, z);
        for (Decorator decorator : generator.getDecorators().getDecorators()) {
            if (decorator.apply(cell, x, z)) {
                break;
            }
        }

        if (biome.getCategory() != Biome.Category.BEACH && biome.getCategory() != Biome.Category.OCEAN) {
            if (cell.continentEdge > 0.4 && cell.continentEdge < 0.45) {
                return false;
            }
        }

        return biomeProvider.getBiome(cell, x, z) == biome;
    }

    protected boolean test(Cell<Terrain> cell, int x, int z) {
        if (biome.getCategory() != Biome.Category.BEACH && biome.getCategory() != Biome.Category.OCEAN) {
            if (cell.continentEdge > 0.4 && cell.continentEdge < 0.5) {
                return false;
            }
        }

        return biomeProvider.getBiome(cell, x, z) == biome;
    }
}
