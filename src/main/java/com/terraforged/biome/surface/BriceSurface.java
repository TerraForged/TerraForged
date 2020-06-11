package com.terraforged.biome.surface;

import com.terraforged.api.chunk.surface.Surface;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.core.Seed;
import com.terraforged.core.util.Variance;
import com.terraforged.world.geology.Strata;
import com.terraforged.n2d.Module;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.util.NoiseUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;

import java.util.Random;

public class BriceSurface implements Surface {

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
    public void buildSurface(int x, int z, int height, SurfaceContext ctx) {
        float alpha = 1 - ctx.cell.steepness;
        float mask = alpha * ctx.cell.biomeEdge * NoiseUtil.map(ctx.cell.riverMask,0, 0.0005F, 0.0005F);
        float value = module.getValue(x, z) * mask;
        int top = (int) (value * 30);

        stackStrata.downwards(x, top, z, ctx.depthBuffer.get(), (y, material) -> {
            if (y <= 0) {
                return false;
            }
            ctx.chunk.setBlockState(ctx.pos.setPos(x, height + y, z), material, false);
            return true;
        });
    }
}
