package com.terraforged.biome.surface;

import com.terraforged.api.biome.surface.Surface;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.func.CellFunc;
import net.minecraft.block.BlockState;

public class SteppeSurface implements Surface {

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        Module module = Source.cell(123, 4, CellFunc.DISTANCE).warp(214, 80, 1, 40);
        float value = module.getValue(x, z);

        BlockState state = States.SAND.get();

        if (value > 0.275F) {
            state = States.GRASS_BLOCK.get();
        } else if (value > 0.225F) {
            state = States.DIRT.get();
        }

        ctx.chunk.setBlockState(ctx.pos.setPos(x, height, z), state, false);
        ctx.chunk.setBlockState(ctx.pos.setPos(x, height-1, z), state, false);
        ctx.chunk.setBlockState(ctx.pos.setPos(x, height-2, z), state, false);
    }
}
