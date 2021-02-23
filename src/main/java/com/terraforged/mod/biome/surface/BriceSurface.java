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

import com.terraforged.engine.Seed;
import com.terraforged.engine.util.Variance;
import com.terraforged.engine.world.geology.Geology;
import com.terraforged.engine.world.geology.Strata;
import com.terraforged.engine.world.geology.Stratum;
import com.terraforged.mod.api.biome.surface.MaskedSurface;
import com.terraforged.mod.api.biome.surface.SurfaceContext;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Random;

public class BriceSurface implements MaskedSurface, Stratum.Visitor<BlockState, SurfaceContext> {

    public static final int SEED_OFFSET = 12341341;

    private final Module module;
    private final Geology<BlockState> stratas;

    public BriceSurface(Seed seed) {
        Random random = new Random(seed.next());
        Variance thick = Variance.of(0.2, 0.3);
        Variance medium = Variance.of(0.1, 0.2);
        Variance thin = Variance.of(0.1, 0.2);

        module = Source.ridge(seed.next(), 60, 4)
                .clamp(0.8, 0.95).map(0, 1)
                .terrace(1, 0.25, 4, 1)
                .mult(Source.perlin(seed.next(), 4, 1).alpha(0.05));

        stratas = new Geology<>(Source.ZERO);

        for (int i = 0; i < 3; i++) {
            stratas.add(Strata.<BlockState>builder(seed.next(), Source.build(seed.next(), 100, 1))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), medium.next(random))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), thick.next(random))
                    .add(Blocks.BROWN_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.YELLOW_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), thick.next(random))
                    .build());
            stratas.add(Strata.<BlockState>builder(seed.next(), Source.build(seed.next(), 100, 1))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), medium.next(random))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), thick.next(random))
                    .add(Blocks.YELLOW_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), thick.next(random))
                    .build());
            stratas.add(Strata.<BlockState>builder(seed.next(), Source.build(seed.next(), 100, 1))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), medium.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.ORANGE_TERRACOTTA.getDefaultState(), thick.next(random))
                    .add(Blocks.WHITE_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.LIGHT_GRAY_TERRACOTTA.getDefaultState(), thin.next(random))
                    .add(Blocks.TERRACOTTA.getDefaultState(), thick.next(random))
                    .build());
        }
    }

    @Override
    public void buildSurface(int x, int z, int height, float mask, SurfaceContext ctx) {
        float strength = 1 - ctx.cell.gradient;
        float value = module.getValue(x, z) * mask * strength;

        int top = (int) (value * 30);
        if (top <= 0) {
            return;
        }

        ctx.pos.setPos(x, height, z);
        stratas.getStrata(ctx.cell.biomeRegionId).downwards(x, top, z, ctx.depthBuffer.get(), ctx, this);
    }

    @Override
    public boolean visit(int y, BlockState state, SurfaceContext ctx) {
        if (y <= 0) {
            return false;
        } else {
            ctx.pos.setY(ctx.surfaceY + y);
            ctx.chunk.setBlockState(ctx.pos, state, false);
            return true;
        }
    }
}
