package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.mod.feature.decorator.ContextualDecorator;
import com.terraforged.api.feature.decorator.DecorationContext;
import com.terraforged.core.util.fastpoisson.FastPoisson;
import com.terraforged.core.util.fastpoisson.FastPoissonContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Stream;

public abstract class FastPoissonDecorator extends ContextualDecorator<FastPoissonConfig> {

    private static final int SEED_OFFSET = 234523;

    public FastPoissonDecorator() {
        super(FastPoissonConfig.CODEC);
    }

    @Override
    protected Stream<BlockPos> getPositions(WorldDecoratingHelper helper, DecorationContext context, Random random, FastPoissonConfig config, BlockPos pos) {
        IChunk chunk = context.getChunk();
        int seed = context.getGenerator().getContext().seed.get() + SEED_OFFSET;
        int chunkX = chunk.getPos().x;
        int chunkZ = chunk.getPos().z;
        FastPoisson poisson = FastPoisson.LOCAL_POISSON.get();
        try (DensityNoise density = config.getDensityNoise(seed, context)) {
            FastPoissonContext poissonConfig = new FastPoissonContext(config.radius, config.scale, density);
            Stream.Builder<BlockPos> builder = Stream.builder();
            poisson.visit(seed, chunkX, chunkZ, random, poissonConfig, builder, getVisitor(context));
            return builder.build();
        }
    }

    protected abstract FastPoisson.Visitor<Consumer<BlockPos>> getVisitor(DecorationContext context);
}
