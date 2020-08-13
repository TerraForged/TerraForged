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
import com.terraforged.core.util.VariablePredicate;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.block.BlockState;
import net.minecraft.world.chunk.IChunk;

public class CoastDecorator implements ColumnDecorator {

    private final Terrains terrains;
    private final BlockState sand;
    private final BlockState gravel;
    private final VariablePredicate height;

    public CoastDecorator(TerraContext context) {
        int min = context.levels.waterLevel - 5;
        int max = context.levels.waterLevel + 16;
        sand = States.SAND.get();
        gravel = States.GRAVEL.get();
        terrains = context.terrain;
        height = VariablePredicate.height(
                context.seed,
                context.levels,
                min,
                max,
                75,
                1
        );
    }

    @Override
    public void decorate(IChunk chunk, DecoratorContext context, int x, int y, int z) {
        if (context.cell.terrain != terrains.beach) {
            return;
        }

        if (!height.test(context.cell, x, z)) {
            return;
        }

        if (context.cell.temperature < 0.3F) {
            setState(chunk, x, y, z, gravel, false);
        } else {
            setState(chunk, x, y, z, sand, false);
        }
    }
}
