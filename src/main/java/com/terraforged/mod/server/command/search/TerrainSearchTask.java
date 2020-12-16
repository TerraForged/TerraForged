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
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;

public class TerrainSearchTask extends ChunkGeneratorSearch {

    private final Terrain type;
    private final WorldGenerator worldGenerator;
    private final Cell cell = new Cell();

    public TerrainSearchTask(BlockPos center, Terrain type, ChunkGenerator chunkGenerator, WorldGenerator worldGenerator) {
        super(center, 256, chunkGenerator);
        this.type = type;
        this.worldGenerator = worldGenerator;
    }

    @Override
    public int getSpacing() {
        return 20;
    }

    @Override
    public boolean test(BlockPos pos) {
        worldGenerator.getHeightmap().apply(cell, pos.getX(), pos.getZ());
        return cell.terrain == type;
    }
}
