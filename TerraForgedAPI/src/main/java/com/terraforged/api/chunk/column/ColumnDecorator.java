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

package com.terraforged.api.chunk.column;

import com.terraforged.api.chunk.ChunkSurfaceBuffer;
import me.dags.noise.Source;
import me.dags.noise.source.FastSource;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;

public interface ColumnDecorator {

    FastSource variance = (FastSource) Source.perlin(0, 100, 1);
    ThreadLocal<BlockPos.Mutable> pos = ThreadLocal.withInitial(BlockPos.Mutable::new);
    ThreadLocal<BlockPos.Mutable> pos1 = ThreadLocal.withInitial(BlockPos.Mutable::new);

    void decorate(IChunk chunk, ProcessorContext context, int x, int y, int z);

    default void decorate(ChunkSurfaceBuffer buffer, ProcessorContext context, int x, int y, int z) {
        decorate(buffer.getChunk(), context, x, y, z);
    }

    default void fillDown(IChunk chunk, int x, int z, int from, int to, BlockState state) {
        for (int dy = from; dy > to; dy--) {
            chunk.setBlockState(pos.get().setPos(x, dy, z), state, false);
        }
    }

    static float getNoise(float x, float z, int seed, float scale, float bias) {
        return (variance.getValue(x, z, seed) * scale) + bias;
    }

    static float getNoise(float x, float z, int seed, int scale, int bias) {
        return getNoise(x, z, seed, scale / 255F, bias / 255F);
    }
}
