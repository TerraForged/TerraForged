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

package com.terraforged.mod.server.command.search;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.Heightmap;

public abstract class ChunkGeneratorSearch extends Search {

    private final ChunkGenerator chunkGenerator;

    public ChunkGeneratorSearch(BlockPos center, ChunkGenerator chunkGenerator) {
        super(center);
        this.chunkGenerator = chunkGenerator;
    }

    public ChunkGeneratorSearch(BlockPos center, int minRadius, ChunkGenerator chunkGenerator) {
        this(center, minRadius, MAX_RADIUS, chunkGenerator);
    }

    public ChunkGeneratorSearch(BlockPos center, int minRadius, int maxRadius, ChunkGenerator chunkGenerator) {
        super(center, minRadius, maxRadius);
        this.chunkGenerator = chunkGenerator;
    }

    @Override
    public BlockPos success(BlockPos.Mutable pos) {
        pos.setY(chunkGenerator.getHeight(pos.getX(), pos.getZ(), Heightmap.Type.WORLD_SURFACE_WG));
        return pos;
    }
}
