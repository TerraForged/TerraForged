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
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class SwampSurface implements Surface {

    private final Module noise;
    private final SurfaceBuilderConfig config = SurfaceBuilder.CONFIG_GRASS;

    public SwampSurface(GeneratorContext context) {
        noise = Source.simplex(context.seed.next(), 40, 2).warp(Source.RAND, context.seed.next(), 2, 1, 4);
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        double noise = Biome.BIOME_INFO_NOISE.getValue(x * 0.25D, z * 0.25D, false);

        if (noise > 0.0D) {
            int dx = x & 15;
            int dz = z & 15;
            for (int y = height; y >= height - 10; --y) {
                ctx.pos.set(dx, y, dz);
                if (ctx.buffer.getBlockState(ctx.pos).isAir()) {
                    continue;
                }

                if (y == ctx.levels.waterY && ctx.buffer.getBlockState(ctx.pos).getBlock() != ctx.fluid.getBlock()) {
                    ctx.buffer.setBlockState(ctx.pos, ctx.fluid, false);
                }
                break;
            }
        }

        SurfaceBuilder.DEFAULT.apply(ctx.random, ctx.buffer, ctx.biome, x, z, height, noise, ctx.solid, ctx.fluid, ctx.levels.waterLevel, ctx.seed, config);

        int y = ctx.chunk.getHeight(Heightmap.Type.OCEAN_FLOOR_WG, x, z);
        if (y <= ctx.levels.waterY) {
            ctx.buffer.setBlockState(ctx.pos.set(x, y, z), getMaterial(x, y, z, ctx), false);
        }
    }

    private BlockState getMaterial(int x, int y, int z, SurfaceContext ctx) {
        float value = noise.getValue(0, x, z);
        if (value > 0.6) {
            if (value < 0.75 && y < ctx.levels.waterY) {
                return States.CLAY.get();
            }
            return States.GRAVEL.get();
        }
        return States.DIRT.get();
    }
}
