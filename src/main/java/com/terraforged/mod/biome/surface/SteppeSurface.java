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

package com.terraforged.mod.biome.surface;

import com.terraforged.mod.api.biome.surface.Surface;
import com.terraforged.mod.api.biome.surface.SurfaceContext;
import com.terraforged.mod.api.material.state.States;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.noise.func.CellFunc;
import net.minecraft.block.BlockState;

public class SteppeSurface implements Surface {

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        Module module = Source.cell(123, 4, CellFunc.DISTANCE).warp(214, 80, 1, 40);
        float value = module.getValue(0, x, z);

        BlockState state = States.SAND.get();

        if (value > 0.275F) {
            state = States.GRASS_BLOCK.get();
        } else if (value > 0.225F) {
            state = States.DIRT.get();
        }

        ctx.chunk.setBlockState(ctx.pos.set(x, height, z), state, false);
        ctx.chunk.setBlockState(ctx.pos.set(x, height-1, z), state, false);
        ctx.chunk.setBlockState(ctx.pos.set(x, height-2, z), state, false);
    }
}
