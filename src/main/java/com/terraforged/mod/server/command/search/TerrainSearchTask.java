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

package com.terraforged.mod.server.command.search;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.WorldGenerator;
import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.server.command.search.condition.SearchCondition;
import com.terraforged.mod.server.command.search.condition.TerrainConditions;
import it.unimi.dsi.fastutil.longs.LongArraySet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;

public class TerrainSearchTask extends ChunkGeneratorSearch {

    private final SearchCondition condition;
    private final WorldGenerator worldGenerator;

    private final Cell cell = new Cell();
    private final LongSet visited = new LongArraySet(2048);

    public TerrainSearchTask(BlockPos center, Terrain type, ChunkGenerator chunkGenerator, WorldGenerator worldGenerator) {
        super(center, 256, chunkGenerator);
        this.worldGenerator = worldGenerator;
        this.condition = TerrainConditions.get(type, worldGenerator.getHeightmap());

        // exclude current terrain region
        worldGenerator.getHeightmap().getRegionModule().apply(cell, center.getX(), center.getZ());
        visited.add(cell.terrainRegionCenter);
    }

    @Override
    public int getSpacing() {
        return 20;
    }

    @Override
    public boolean test(BlockPos pos) {
        worldGenerator.getHeightmap().getRegionModule().apply(cell, pos.getX(), pos.getZ());
        // avoid searching same terrain region twice
        if (visited.add(cell.terrainRegionCenter)) {
            return condition.test(cell, pos.getX(), pos.getZ());
        }
        return false;
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        return super.success(condition.complete(pos));
    }
}
