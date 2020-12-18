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

import com.terraforged.mod.api.biome.surface.MaskedSurface;
import com.terraforged.mod.api.biome.surface.SurfaceContext;
import com.terraforged.mod.api.material.state.States;
import com.terraforged.engine.Seed;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.world.gen.Heightmap;

import java.util.function.IntFunction;

public class StoneForestSurface implements MaskedSurface {

    private final Module module;
    private final BlockState dirt;
    private final BlockState grass;
    private final BlockState stone;

    public StoneForestSurface(Seed seed) {
        dirt = States.DIRT.get();
        stone = States.STONE.get();
        grass = States.GRASS_BLOCK.get();

        IntFunction<Module> func = scale ->Source.ridge(seed.next(), scale, 4).clamp(0.7, 0.95).map(0, 1)
                .pow(1.5)
                .terrace(1, 0.25, 4, 1);

        module = func.apply(50).max(func.apply(55));
    }

    @Override
    public void buildSurface(int x, int z, int height, float mask, SurfaceContext ctx) {
        // reduce height on steeper terrain
        float strength = 1 - ctx.cell.gradient;
        float value = module.getValue(x, z) * mask * strength;

        int top = height + (int) (value * 50);
        if (top > height) {
            height = ctx.chunk.getTopBlockY(Heightmap.Type.OCEAN_FLOOR_WG, x, z);

            for (int y = height; y < top - 1; y++) {
                ctx.buffer.setBlockState(ctx.pos.setPos(x, y, z), stone, false);
            }

            ctx.buffer.setBlockState(ctx.pos.setPos(x, top, z), grass, false);
            ctx.buffer.setBlockState(ctx.pos.setPos(x, top - 1, z), dirt, false);
        }
    }
}
