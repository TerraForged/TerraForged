package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.api.feature.decorator.DecorationContext;
import com.terraforged.core.util.fastpoisson.FastPoisson;
import com.terraforged.mod.TerraForgedMod;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.Heightmap;

import java.util.function.Consumer;

public class FastPoissonAtSurface extends FastPoissonDecorator {

    public static final FastPoissonAtSurface INSTANCE = new FastPoissonAtSurface();

    private FastPoissonAtSurface() {
        setRegistryName(TerraForgedMod.MODID, "fast_poisson_surface");
    }

    @Override
    protected FastPoisson.Visitor<Consumer<BlockPos>> getVisitor(DecorationContext context) {
        return (x, z, builder) -> {
            int y = context.getRegion().getHeight(Heightmap.Type.WORLD_SURFACE, x, z);
            builder.accept( new BlockPos(x, y, z));
        };
    }
}
