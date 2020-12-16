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

package com.terraforged.api.chunk.column;

import com.terraforged.api.chunk.ChunkContext;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.world.climate.Climate;
import com.terraforged.engine.world.geology.DepthBuffer;
import com.terraforged.engine.world.heightmap.Levels;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;

public class DecoratorContext extends ChunkContext implements AutoCloseable {

    public final Levels levels;
    public final Climate climate;
    public final Resource<DepthBuffer> depthBuffer;
    public final BlockPos.Mutable pos = new BlockPos.Mutable();

    public Biome biome;
    public Cell cell;

    public DecoratorContext(IChunk chunk, Levels levels, Climate climate) {
        this(chunk, levels, climate, true);
    }

    public DecoratorContext(IChunk chunk, Levels levels, Climate climate, boolean depthBuffer) {
        super(chunk);
        this.levels = levels;
        this.climate = climate;
        this.depthBuffer = depthBuffer ? DepthBuffer.get() : null;
    }

    @Override
    public void close() {
        if (depthBuffer != null) {
            depthBuffer.close();
        }
    }
}
