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

import com.terraforged.api.biome.surface.MaskedSurface;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.core.Seed;
import com.terraforged.core.util.Variance;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.geology.Strata;
import com.terraforged.world.geology.Stratum;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Random;

public class BriceSurface implements MaskedSurface, Stratum.Visitor<BlockState, SurfaceContext> {

    private final Module module;
    private final Strata<BlockState> stackStrata;

    public BriceSurface(Seed seed) {
        Random random = new Random(seed.next());
        Variance variance = Variance.of(0.1, 0.2);

        module = Source.ridge(123, 60, 4)
                .clamp(0.8, 0.95).map(0, 1)
                .terrace(1, 0.25, 4, 1)
                .mult(Source.perlin(1234, 4, 1).alpha(0.05));

        stackStrata = Strata.<BlockState>builder(seed.next(), Source.build(seed.next(), 30, 3))
                .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), variance.next(random))
                .add(Blocks.TERRACOTTA.getDefaultState(), variance.next(random))
                .add(Blocks.YELLOW_TERRACOTTA.getDefaultState(), variance.next(random))
                .add(Blocks.WHITE_TERRACOTTA.getDefaultState(), variance.next(random))
                .add(Blocks.BROWN_TERRACOTTA.getDefaultState(), variance.next(random))
                .add(Blocks.RED_TERRACOTTA.getDefaultState(), variance.next(random))
                .build();
    }

    @Override
    public void buildSurface(int x, int z, int height, float mask, SurfaceContext ctx) {
        float strength = 1 - ctx.cell.gradient;
        float value = module.getValue(x, z) * mask * strength;

        int top = (int) (value * 30);
        if (top == 0) {
            return;
        }

        stackStrata.downwards(x, top, z, ctx.depthBuffer.get(), ctx, this);
    }

    @Override
    public boolean visit(int y, BlockState state, SurfaceContext ctx) {
        if (y <= 0) {
            return false;
        } else {
            ctx.pos.setY(y);
            ctx.chunk.setBlockState(ctx.pos, state, false);
            return true;
        }
    }
}
