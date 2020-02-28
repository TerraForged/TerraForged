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
import com.terraforged.core.world.WorldGenerator;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.mod.biome.provider.BiomeProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

public class FindBothTask extends FindBiomeTask {

    private final Terrain type;
    private final WorldGenerator generator;
    private final Cell<Terrain> cell = new Cell<>();

    public FindBothTask(BlockPos center, Terrain terrain, Biome biome, WorldGenerator generator, BiomeProvider biomeProvider) {
        super(center, biome, generator, biomeProvider, 300);
        this.type = terrain;
        this.generator = generator;
    }

    @Override
    protected boolean search(int x, int z) {
        generator.getHeightmap().apply(cell, x, z);
        return test(cell, x , z) && super.test(cell, x, z);
    }

    @Override
    protected boolean test(Cell<Terrain> cell, int x, int z) {
        return cell.continentEdge > 0.5 && cell.regionEdge > 0.8F && cell.tag == type;
    }
}
