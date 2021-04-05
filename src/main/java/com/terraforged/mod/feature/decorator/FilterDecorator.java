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

import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.api.feature.decorator.DecorationContext;
import com.terraforged.mod.feature.decorator.filter.FilterDecoratorConfig;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

import java.util.Random;
import java.util.stream.Stream;

public class FilterDecorator extends ContextualDecorator<FilterDecoratorConfig> {

    public static final FilterDecorator INSTANCE = new FilterDecorator();

    private FilterDecorator() {
        super(FilterDecoratorConfig.CODEC);
        setRegistryName(TerraForgedMod.MODID, "filter");
    }

    @Override
    protected Stream<BlockPos> getPositions(WorldDecoratingHelper helper, DecorationContext context, Random random, FilterDecoratorConfig config, BlockPos pos) {
        return config.placement.getPositions(helper, random, pos).filter(p -> config.filter.test(context, p));
    }
}
