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

package com.terraforged.mod.api.biome.surface;


import com.terraforged.mod.api.biome.surface.builder.Combiner;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.IChunk;

public interface Surface {

    void buildSurface(int x, int z, int height, SurfaceContext ctx);

    default void fill(int x, int z, int start, int end, SurfaceContext ctx, IChunk chunk, BlockState state) {
        if (start < end) {
            for (int y = start; y < end; y++) {
                chunk.setBlockState(ctx.pos.set(x, y, z), state, false);
            }
        } else if (start > end) {
            for (int y = start; y > end; y--) {
                chunk.setBlockState(ctx.pos.set(x, y, z), state, false);
            }
        }
    }

    default Surface then(Surface next) {
        return new Combiner(this, next);
    }
}
