package com.terraforged.feature.decorator.poisson;

import com.terraforged.core.util.poisson.Poisson;
import com.terraforged.core.util.poisson.PoissonContext;
import me.dags.noise.Module;
import me.dags.noise.Source;
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
    public final <FC extends IFeatureConfig, F extends Feature<FC>> boolean place(IWorld world, ChunkGenerator<?> generator, Random random, BlockPos pos, PoissonConfig config, ConfiguredFeature<FC, F> feature) {
        int radius = Math.max(1, Math.min(30, config.radius));
        Poisson poisson = getInstance(radius);
        PoissonVisitor visitor = new PoissonVisitor(this, feature, world, generator, random, pos);
        setVariance(world, visitor, config);
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

    private void setVariance(IWorld world, PoissonContext context, PoissonConfig config) {
        Module module = Source.ONE;

        if (config.biomeFade > 0.075F) {
            module = BiomeVariance.of(world, config.biomeFade);
        }

        if (config.variance > 0) {
            module = module.mult(Source.simplex(context.seed, config.variance, 1)
                    .scale(config.max - config.min)
                    .bias(config.min));
        }

        context.density = module;
    }
}
