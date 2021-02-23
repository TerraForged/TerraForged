/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.feature.decorator;

import com.mojang.serialization.Codec;
import com.terraforged.mod.api.feature.decorator.DecorationContext;
import com.terraforged.mod.feature.DecorationAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;
import net.minecraft.world.gen.placement.IPlacementConfig;
import net.minecraft.world.gen.placement.Placement;

import java.util.Random;
import java.util.stream.Stream;

public abstract class ContextualDecorator<T extends IPlacementConfig> extends Placement<T> {
    
    public ContextualDecorator(Codec<T> codec) {
        super(codec);
    }

    @Override
    public Stream<BlockPos> getPositions(WorldDecoratingHelper helper, Random random, T config, BlockPos pos) {
        DecorationContext context = DecorationAccessor.getContext(helper);
        if (context == null) {
            new UnsupportedOperationException().printStackTrace();
            return Stream.empty();
        }
        return getPositions(helper, context, random, config, pos);
    }
    
    protected abstract Stream<BlockPos> getPositions(WorldDecoratingHelper helper, DecorationContext context, Random random, T config, BlockPos pos);
}
