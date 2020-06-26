package com.terraforged.mod.feature.decorator.poisson;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;

import java.util.Random;

public class PoissonAtSurface extends PoissonDecorator {

    public PoissonAtSurface() {
        setRegistryName("terraforged", "poisson_surface");
    }

    @Override
    public int getYAt(IWorld world, BlockPos pos, Random random) {
        return world.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos.getX(), pos.getZ());
    }
}
