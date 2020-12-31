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

import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.api.material.state.States;
import net.minecraft.world.chunk.IChunk;

public class BaseDecorator implements ColumnDecorator {

    public static final BaseDecorator INSTANCE = new BaseDecorator();

    private static final int LAVA_OFFSET = 16;
    private static final int LAVA_LEVEL_MIN = 24;
    private static final int LAVA_LEVEL_RESOLUTION = 4;
    private static final float RIVER_MASK_THRESHOLD = 0.85F;

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        y = decorateFluid(chunk, context, x, y, z);

        fillDown(context, chunk, x, z, y, 0, States.STONE.get());
    }

    protected int decorateFluid(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (y < context.levels.waterLevel) {
            return fillDown(context, chunk, x, z, context.levels.waterY, y, States.WATER.get());
        }

        if (context.cell.terrain == TerrainType.VOLCANO_PIPE && context.cell.riverMask > RIVER_MASK_THRESHOLD) {
            int lavaLevel = getLavaLevel(y, context.levels);

            if (lavaLevel > 0) {
                int lavaEnd = Math.max(5, context.levels.waterY - LAVA_OFFSET);

                if (lavaLevel > lavaEnd) {
                    return fillDown(context, chunk, x, z, lavaLevel, lavaEnd, States.LAVA.get());
                }
            }
        }

        return y;
    }

    private static int getLavaLevel(int y, Levels levels) {
        int dy = y - levels.waterLevel;

        // surface must be at-least LAVA_OFFSET above water level
        if (dy <= LAVA_OFFSET) {
            return 0;
        }

        // lava must start at-least LAVA_OFFSET below surface
        dy -= LAVA_OFFSET;

        // round dy to nearest lava level
        int level = (dy >> LAVA_LEVEL_RESOLUTION) << LAVA_LEVEL_RESOLUTION;

        // lava level must be at least LAVA_LEVEL_MIN (otherwise it indicates the surface is tool close to sea level)
        if (level <= LAVA_LEVEL_MIN) {
            return 0;
        }

        return levels.waterLevel + level;
    }
}
