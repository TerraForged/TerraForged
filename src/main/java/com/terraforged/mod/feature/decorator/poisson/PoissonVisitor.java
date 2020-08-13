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
import com.terraforged.core.util.poisson.PoissonContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;
import java.util.function.Consumer;

public class PoissonVisitor extends PoissonContext implements Poisson.Visitor, Consumer<Consumer<? super BlockPos>> {

    private final BlockPos pos;
    private final ISeedReader world;
    private final Poisson poisson;
    private final PoissonDecorator decorator;
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    private final int chunkX;
    private final int chunkZ;

    private Consumer<? super BlockPos> consumer;

    public PoissonVisitor(ISeedReader world, PoissonDecorator decorator, Poisson poisson, Random random, BlockPos pos) {
        super(world.getSeed(), random);
        this.pos = pos;
        this.world = world;
        this.decorator = decorator;
        this.chunkX = pos.getX() >> 4;
        this.chunkZ = pos.getZ() >> 4;
        this.poisson = poisson;
    }

    @Override
    public void visit(int x, int z) {
        mutable.setPos(x, pos.getY(), z);
        mutable.setY(decorator.getYAt(world, mutable, random));
        consumer.accept(mutable);
    }

    @Override
    public void accept(Consumer<? super BlockPos> consumer) {
        this.consumer = consumer;
        this.poisson.visit(chunkX, chunkZ, this, this);
        this.consumer = null;
    }
}
