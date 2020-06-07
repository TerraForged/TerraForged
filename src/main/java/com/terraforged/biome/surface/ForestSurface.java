package com.terraforged.biome.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.world.GeneratorContext;
import me.dags.noise.Module;
import me.dags.noise.Source;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

public class ForestSurface implements Surface {

    private final Module noise;

    public ForestSurface(GeneratorContext context) {
        noise = Source.simplex(context.seed.next(), 40, 2).warp(Source.RAND, context.seed.next(), 2, 1, 4);
    }

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        BlockState state = getMaterial(x, z);
        ctx.buffer.setBlockState(ctx.pos.setPos(x, height, z), state, false);
    }

    private BlockState getMaterial(int x, int z) {
        float value = noise.getValue(x, z);
        if (value > 0.6) {
            if (value < 0.75) {
                return Blocks.PODZOL.getDefaultState();
            }
            return States.DIRT.get();
        }
        return States.GRASS_BLOCK.get();
    }
}
