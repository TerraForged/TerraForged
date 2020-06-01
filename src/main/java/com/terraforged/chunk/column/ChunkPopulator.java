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

package com.terraforged.chunk.column;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.material.state.States;
import net.minecraft.world.chunk.IChunk;

public class ChunkPopulator implements ColumnDecorator {

    public static final ChunkPopulator INSTANCE = new ChunkPopulator();

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (context.cell.terrain == context.terrains.volcanoPipe && context.cell.riverMask > 0.25F) {
            int lavaStart = Math.max(context.levels.waterY + 10, y - 30);
            int lavaEnd = Math.max(5, context.levels.waterY - 10);
            fillDown(context, chunk, x, z, lavaStart, lavaEnd, States.LAVA.get());
            y = lavaEnd;
        } else if (y < context.levels.waterLevel) {
            fillDown(context, chunk, x, z, context.levels.waterY, y, States.WATER.get());
        }
        fillDown(context, chunk, x, z, y, 0, States.STONE.get());
    }
}
