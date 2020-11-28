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
