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

package com.terraforged.mod.feature.context;

import com.terraforged.core.concurrent.Resource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.Feature;

import java.util.Random;

public class ContextSelectorFeature extends Feature<ContextSelectorConfig> {

    public static final ContextSelectorFeature INSTANCE = new ContextSelectorFeature();

    public ContextSelectorFeature() {
        super(ContextSelectorConfig.CODEC);
        setRegistryName("terraforged", "context_selector");
    }

    @Override
    public boolean func_241855_a(ISeedReader world, ChunkGenerator generator, Random random, BlockPos pos, ContextSelectorConfig config) {
        try (Resource<ChanceContext> item = ChanceContext.pooled(world, generator)) {
            if (item == null) {
                return false;
            }

            ChanceContext context = item.get();
            context.setPos(pos);
            context.init(config.features.size());
            for (int i = 0; i < config.features.size(); i++) {
                ContextualFeature feature = config.features.get(i);
                float chance = feature.getChance(pos, context);
                context.record(i, chance);
            }

            int index = context.nextIndex(random);
            if (index > -1) {
                return config.features.get(index).feature.func_242765_a(world, generator, random, pos);
            }

            return false;
        }
    }
}
