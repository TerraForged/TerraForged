/*
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

package com.terraforged.mod.biome.spawn;

import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.server.command.search.Search;
import net.minecraft.util.math.BlockPos;

public class SpawnSearch extends Search {

    private final TerraBiomeProvider biomeProvider;
    private final Cell cell = new Cell();

    public SpawnSearch(BlockPos center, TerraBiomeProvider biomeProvider) {
        super(center, 0, 2048);
        this.biomeProvider = biomeProvider;
    }

    @Override
    public int getSpacing() {
        return 64;
    }

    @Override
    public boolean test(BlockPos pos) {
        biomeProvider.getWorldLookup().applyCell(cell, pos.getX(), pos.getZ());
        return biomeProvider.canSpawnAt(cell);
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(biomeProvider.getContext().levels.scale(cell.value));
        Log.info("Found valid spawn position: {}", pos);
        return super.success(pos);
    }

    @Override
    public BlockPos fail(BlockPos pos) {
        Log.info("Unable to find valid spawn position, defaulting x=0, z=0");
        return new BlockPos(0, biomeProvider.getContext().levels.groundLevel, 0);
    }
}
