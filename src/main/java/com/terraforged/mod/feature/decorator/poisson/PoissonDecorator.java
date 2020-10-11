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

package com.terraforged.mod.feature.decorator.poisson;

import com.terraforged.core.util.poisson.Poisson;
import com.terraforged.mod.util.stream.PosStream;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.Placement;

import java.lang.reflect.Field;
import java.util.Random;
import java.util.stream.Stream;

public abstract class PoissonDecorator extends Placement<PoissonConfig> {

    private static final HelperAccess ACCESS = new HelperAccess();

    private Poisson instance = null;
    private final Object lock = new Object();

    public PoissonDecorator() {
        super(PoissonConfig.CODEC);
    }

    @Override
    public final Stream<BlockPos> func_241857_a(WorldDecoratingHelper helper, Random random, PoissonConfig config, BlockPos pos) {
        int radius = Math.max(1, Math.min(30, config.radius));
        Poisson poisson = getInstance(radius);
        ISeedReader world = ACCESS.getWorld(helper);
        ChunkGenerator generator = ACCESS.getGenerator(helper);
        PoissonVisitor visitor = new PoissonVisitor(world, this, poisson, random, pos);
        config.apply(world, generator, visitor);
        return visitor.stream();
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

    private static class HelperAccess {

        private final Field world;
        private final Field generator;

        private HelperAccess() {
            Field world = null;
            Field generator = null;

            for (Field field : WorldDecoratingHelper.class.getDeclaredFields()) {
                if (field.getType() == ISeedReader.class) {
                    field.setAccessible(true);
                    world = field;
                } else if (field.getType() == ChunkGenerator.class) {
                    field.setAccessible(true);
                    generator = field;
                }
            }

            if (world == null || generator == null) {
                throw new RuntimeException();
            }

            this.world = world;
            this.generator = generator;
        }

        private ISeedReader getWorld(WorldDecoratingHelper helper) {
            try {
                return (ISeedReader) world.get(helper);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }

        private ChunkGenerator getGenerator(WorldDecoratingHelper helper) {
            try {
                return (ChunkGenerator) generator.get(helper);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
