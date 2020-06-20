package com.terraforged.biome.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.world.GeneratorContext;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class DesertSurface implements Surface {

    private final float min;
    private final float level;
    private final Module noise;
    private final BlockState sandstone = States.SANDSTONE.get();
    private final BlockState low = Blocks.TERRACOTTA.delegate.get().getDefaultState();
    private final BlockState mid = Blocks.ORANGE_TERRACOTTA.delegate.get().getDefaultState();
    private final BlockState high = Blocks.BROWN_TERRACOTTA.delegate.get().getDefaultState();

    public DesertSurface(GeneratorContext context) {
        min = context.levels.ground(10);

        level = context.levels.ground(40);

        noise = Source.perlin(context.seed.next(), 8, 1)
                .scale(context.levels.scale(16));
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        if (ctx.cell.steepness < 0.15) {
            return;
        }

        if (ctx.cell.value < min) {
            return;
        }

        float value = ctx.cell.value + noise.getValue(x, z);
        if (ctx.cell.steepness > 0.3 || value > level) {
            BlockState state = sandstone;

            if (value > level) {
                if (ctx.cell.steepness > 0.975) {
                    state = low;
                } else if (ctx.cell.steepness > 0.85) {
                    state = high;
                } else if (ctx.cell.steepness > 0.75) {
                    state = mid;
                } else if (ctx.cell.steepness > 0.65) {
                    state = low;
                }
            }

            for (int dy = 0; dy < 4; dy++) {
                ctx.buffer.setBlockState(ctx.pos.setPos(x, height - dy, z), state, false);
            }
        }
    }
}
