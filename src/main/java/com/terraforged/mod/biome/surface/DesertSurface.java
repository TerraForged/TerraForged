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

import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.mod.api.biome.surface.Surface;
import com.terraforged.mod.api.biome.surface.SurfaceContext;
import com.terraforged.mod.api.material.state.States;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class DesertSurface implements Surface {

    private final float min;
    private final float level;
    private final Module noise;
    private final BlockState sandstone = States.SMOOTH_SANDSTONE.get();
    private final BlockState low = Blocks.TERRACOTTA.delegate.get().defaultBlockState();
    private final BlockState mid = Blocks.ORANGE_TERRACOTTA.delegate.get().defaultBlockState();
    private final BlockState high = Blocks.BROWN_TERRACOTTA.delegate.get().defaultBlockState();

    public DesertSurface(GeneratorContext context) {
        min = context.levels.ground(10);

        level = context.levels.ground(40);

        noise = Source.perlin(context.seed.next(), 8, 1)
                .scale(context.levels.scale(16));
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        if (ctx.cell.gradient < 0.15) {
            return;
        }

        if (ctx.cell.value < min) {
            return;
        }

        float value = ctx.cell.value + noise.getValue(x, z);
        if (ctx.cell.gradient > 0.3 || value > level) {
            BlockState state = sandstone;

            if (value > level) {
                if (ctx.cell.gradient > 0.975) {
                    state = low;
                } else if (ctx.cell.gradient > 0.85) {
                    state = high;
                } else if (ctx.cell.gradient > 0.75) {
                    state = mid;
                } else if (ctx.cell.gradient > 0.65) {
                    state = low;
                }
            }

            for (int dy = 0; dy < 4; dy++) {
                ctx.buffer.setBlockState(ctx.pos.set(x, height - dy, z), state, false);
            }
        }
    }
}
