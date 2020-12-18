/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.engine.util.fastpoisson.FastPoisson;
import com.terraforged.engine.util.fastpoisson.FastPoissonContext;
import com.terraforged.mod.api.feature.decorator.DecorationContext;
import com.terraforged.mod.feature.decorator.ContextualDecorator;
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
