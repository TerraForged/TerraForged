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

package com.terraforged.mod.biome.surface;

import com.terraforged.api.biome.surface.Surface;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import com.terraforged.engine.world.GeneratorContext;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.Heightmap;

public class ForestSurface implements Surface {

    private final Module noise;

    public ForestSurface(GeneratorContext context) {
        noise = Source.simplex(context.seed.next(), 50, 2).warp(Source.RAND, context.seed.next(), 2, 1, 3);
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        if (ctx.buffer.getTopBlockY(Heightmap.Type.OCEAN_FLOOR_WG, x, z) == height) {
            BlockState state = getMaterial(x, z);
            ctx.buffer.setBlockState(ctx.pos.setPos(x, height, z), state, false);
        }
    }

    private BlockState getMaterial(int x, int z) {
        float value = noise.getValue(x, z);
        if (value > 0.65) {
            if (value < 0.725) {
                return States.PODZOL.get();
            }
            return States.DIRT.get();
        }
        return States.GRASS_BLOCK.get();
    }
}
