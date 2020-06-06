package com.terraforged.feature.decorator.poisson;

import com.terraforged.core.util.poisson.Poisson;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public abstract class PoissonDecorator extends Placement<PoissonConfig> {

    private Poisson instance = null;
    private final Object lock = new Object();

    public PoissonDecorator() {
        super(PoissonConfig::deserialize);
    }

    @Override
    public <FC extends IFeatureConfig, F extends Feature<FC>> boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, PoissonConfig config, ConfiguredFeature<FC, F> feature) {
        int radius = Math.max(1, Math.min(30, config.radius));
        Poisson poisson = getInstance(radius);
        PoissonVisitor visitor = new PoissonVisitor(this, feature, world, generator, random, pos);
        config.apply(world, generator, visitor);
        int chunkX = pos.getX() >> 4;
        int chunkZ = pos.getZ() >> 4;
        poisson.visit(chunkX, chunkZ, visitor, visitor);
        return visitor.hasPlacedOne();
    }

    @Override
    public final Stream<BlockPos> getPositions(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generatorIn, Random random, PoissonConfig configIn, BlockPos pos) {
        return Stream.empty();
    }

    public abstract int getYAt(IWorld world, BlockPos pos, Random random);

    private Poisson getInstance(int radius) {
        synchronized (lock) {
            if (instance == null || instance.getRadius() != radius) {
                instance = new Poisson(radius);
            }
            return instance;
        }
    }
}
