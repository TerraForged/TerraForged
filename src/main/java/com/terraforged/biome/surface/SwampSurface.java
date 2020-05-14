package com.terraforged.biome.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilder;
import net.minecraft.world.gen.surfacebuilders.SurfaceBuilderConfig;

public class SwampSurface implements Surface {

    private final SurfaceBuilderConfig config = SurfaceBuilder.GRASS_DIRT_GRAVEL_CONFIG;

    @Override
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        double noise = Biome.INFO_NOISE.noiseAt(x * 0.25D, z * 0.25D, false);
        if (noise > 0.0D) {
            int dx = x & 15;
            int dz = z & 15;
            for (int y = height; y >= height - 10; --y) {
                ctx.pos.setPos(dx, y, dz);
                if (ctx.buffer.getBlockState(ctx.pos).isAir()) {
                    continue;
                }

                if (y == ctx.levels.waterY && ctx.buffer.getBlockState(ctx.pos).getBlock() != ctx.fluid.getBlock()) {
                    ctx.buffer.setBlockState(ctx.pos, ctx.fluid, false);
                }
                break;
            }
        }

        SurfaceBuilder.DEFAULT.buildSurface(ctx.random, ctx.buffer, ctx.biome, x, z, height, noise, ctx.solid, ctx.fluid, ctx.levels.waterLevel, ctx.seed, config);
    }
}
