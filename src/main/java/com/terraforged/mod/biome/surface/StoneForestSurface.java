package com.terraforged.mod.biome.surface;

import com.terraforged.api.biome.surface.MaskedSurface;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.material.state.States;
import com.terraforged.core.Seed;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import net.minecraft.block.BlockState;

public class StoneForestSurface implements MaskedSurface {

    private final Module module;
    private final BlockState dirt;
    private final BlockState grass;
    private final BlockState stone;

    public StoneForestSurface(Seed seed) {
        dirt = States.DIRT.get();
        stone = States.STONE.get();
        grass = States.GRASS_BLOCK.get();
        module = Source.ridge(seed.next(), 50, 4)
                .clamp(0.7, 0.95).map(0, 1)
                .pow(1.5)
                .terrace(1, 0.25, 4, 1);
    }

    @Override
    public void buildSurface(int x, int z, int height, float mask, SurfaceContext ctx) {
        // reduce height on steeper terrain
        float strength = 1 - ctx.cell.gradient;
        float value = module.getValue(x, z) * mask * strength;

        int top = height + (int) (value * 50);
        if (top > height) {
            for (int y = height; y < top - 1; y++) {
                ctx.buffer.setBlockState(ctx.pos.setPos(x, y, z), stone, false);
            }

            ctx.buffer.setBlockState(ctx.pos.setPos(x, top, z), grass, false);
            ctx.buffer.setBlockState(ctx.pos.setPos(x, top - 1, z), dirt, false);
        }
    }
}
