/*
 *
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

package com.terraforged.mod.decorator.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.heightmap.Levels;
import com.terraforged.mod.chunk.TerraContext;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;

public class FrozenOcean implements Surface {

    private final Module up;
    private final Module down;
    private final Module fadeUp;
    private final Module bergTop;
    private final Module seaFloor;
    private final Levels levels;

    public FrozenOcean(TerraContext context, int height, int depth) {
        Levels levels = context.levels;
        Module shape = Source.perlin(context.seed.next(), 65, 3)
                .warp(context.seed.next(), 15, 1, 10)
                .cache();
        Module mask = shape.threshold(0.6).cache();
        Module fadeDown = shape.clamp(0.6, 0.725).map(0, 1);

        this.levels = levels;

        this.fadeUp = shape.clamp(0.6, 0.65).map(0, 1);

        this.up = Source.ridge(context.seed.next(), 50, 3)
                .mult(mask)
                .mult(fadeUp)
                .scale(levels.scale(height));

        this.down = Source.ridge(context.seed.next(), 60, 3)
                .mult(mask)
                .mult(fadeDown)
                .scale(levels.scale(depth));

        this.bergTop = Source.perlin(context.seed.next(), 25, 2)
                .scale(levels.scale(3))
                .bias(levels.scale(2));

        this.seaFloor = Source.perlin(context.seed.next(), 50, 1)
                .scale(levels.scale(3))
                .bias(levels.scale(1));
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        int center = levels.waterLevel - 5;
        int top = center + (int) (up.getValue(x, z) * levels.worldHeight);
        int topDepth = (int) (bergTop.getValue(x, z) * levels.worldHeight);
        int bottom = center - (int) (down.getValue(x, z) * levels.worldHeight);

        // set iceberg materials
        BlockPos.Mutable pos = new BlockPos.Mutable(x, height, z);
        for (int dy = top; dy > bottom; dy--) {
            pos.setY(dy);
            BlockState state = getMaterial(x, dy, z, top, topDepth);
            ctx.chunk.setBlockState(pos, state, false);
        }

        // set ocean floor to gravel
        int floorBed = (int) (ctx.cell.value * ctx.levels.worldHeight);
        int floorDepth = (int) (seaFloor.getValue(x, z) * levels.worldHeight);
        for (int dy = 0; dy < floorDepth; dy++) {
            pos.setY(floorBed - dy);
            ctx.chunk.setBlockState(pos, SurfaceBuilder.GRAVEL, false);
        }
    }

    private BlockState getMaterial(int x, int y, int z, int top, int topDepth) {
        if (y >= top - topDepth && fadeUp.getValue(x, z) == 1) {
            return States.SNOW_BLOCK.get();
        }
        return States.PACKED_ICE.get();
    }

    private static float getMask(Cell<?> cell) {
        return NoiseUtil.map(cell.biomeTypeMask * cell.riverMask, 0F, 0.3F, 0.3F);
    }
}
