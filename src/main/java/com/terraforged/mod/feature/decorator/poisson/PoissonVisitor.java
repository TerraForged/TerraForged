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

import com.terraforged.engine.util.poisson.Poisson;
import com.terraforged.engine.util.poisson.PoissonContext;
import com.terraforged.mod.chunk.util.ChunkBitSet;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;

import java.util.Random;
import java.util.stream.Stream;

public class PoissonVisitor extends PoissonContext implements Poisson.Visitor {

    private final ISeedReader world;
    private final Poisson poisson;
    private final PoissonDecorator decorator;

    public final int chunkX;
    public final int chunkZ;

    private final ChunkBitSet chunk = new ChunkBitSet();

    public PoissonVisitor(ISeedReader world, PoissonDecorator decorator, Poisson poisson, Random random, BlockPos pos) {
        super(world.getSeed(), random);
        this.world = world;
        this.poisson = poisson;
        this.decorator = decorator;
        this.chunkX = pos.getX() >> 4;
        this.chunkZ = pos.getZ() >> 4;
    }

    @Override
    public void visit(int x, int z) {
        x -= startX;
        z -= startZ;
        chunk.set(x, z);
    }

    public Stream<BlockPos> stream() {
        poisson.visit(chunkX, chunkZ, this, this);
        return chunk.stream(startX, 0, startZ).map(mutable -> {
            mutable.setY(decorator.getYAt(world, mutable, random));
            return mutable;
        });
    }
}
