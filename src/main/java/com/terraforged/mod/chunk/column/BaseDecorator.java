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

package com.terraforged.mod.chunk.column;

import com.terraforged.api.chunk.column.ColumnDecorator;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.material.state.States;
import com.terraforged.world.terrain.TerrainType;
import net.minecraft.world.chunk.IChunk;

public class BaseDecorator implements ColumnDecorator {

    public static final BaseDecorator INSTANCE = new BaseDecorator();

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        y = decorateFluid(chunk, context, x, y, z);

        fillDown(context, chunk, x, z, y, 0, States.STONE.get());
    }

    protected int decorateFluid(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (context.cell.terrain == TerrainType.VOLCANO_PIPE && context.cell.riverMask > 0.5F) {
            int lavaStart = Math.max(context.levels.waterY + 10, y - 30);
            int lavaEnd = Math.max(5, context.levels.waterY - 10);
            fillDown(context, chunk, x, z, lavaStart, lavaEnd, States.LAVA.get());
            return lavaEnd;
        }

        if (y < context.levels.waterLevel) {
            fillDown(context, chunk, x, z, context.levels.waterY, y, States.WATER.get());
        }
        return y;
    }
}
