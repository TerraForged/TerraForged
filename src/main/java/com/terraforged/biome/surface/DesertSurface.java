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

    private final float level;
    private final Module noise;
    private final BlockState low = Blocks.TERRACOTTA.delegate.get().getDefaultState();
    private final BlockState mid = Blocks.ORANGE_TERRACOTTA.delegate.get().getDefaultState();
    private final BlockState high = Blocks.BROWN_TERRACOTTA.delegate.get().getDefaultState();

    public DesertSurface(GeneratorContext context) {
        level = context.levels.ground(40);

        noise = Source.perlin(context.seed.next(), 10, 1)
                .scale(context.levels.scale(10));
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        if (ctx.cell.steepness < 0.15) {
            return;
        }

        if (ctx.cell.steepness > 0.3 || ctx.cell.value + noise.getValue(x, z) > level) {
            BlockState state = States.SANDSTONE.get();

            if (ctx.cell.value > level) {
                if (ctx.cell.steepness > 0.85) {
                    state = high;
                } else if (ctx.cell.steepness > 0.75) {
                    state = mid;
                } else if (ctx.cell.steepness > 0.625) {
                    state = low;
                }
            }

            for (int dy = 0; dy < 4; dy++) {
                ctx.buffer.setBlockState(ctx.pos.setPos(x, height - dy, z), state, false);
            }
        }
    }
}
