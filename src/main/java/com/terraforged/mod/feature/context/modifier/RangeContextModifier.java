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

package com.terraforged.mod.feature.context.modifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.feature.context.ChanceContext;
import com.terraforged.mod.util.RangeModifier;
import net.minecraft.util.math.BlockPos;

public abstract class RangeContextModifier extends RangeModifier implements ContextModifier {

    public RangeContextModifier(float min, float max, boolean exclusive) {
        super(min, max, exclusive);
    }

    protected abstract float getValue(BlockPos pos, ChanceContext context);

    @Override
    public float getChance(BlockPos pos, ChanceContext context) {
        return apply(getValue(pos, context));
    }

    @Override
    public <T> Dynamic<T> serialize(DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("from"), ops.createFloat(from),
                ops.createString("to"), ops.createFloat(to),
                ops.createString("exclusive"), ops.createBoolean(max == 0)
        )));
    }

    public static <T extends RangeContextModifier> T deserialize(Dynamic<?> dynamic, Factory<T> factory) {
        float from = dynamic.get("from").asFloat(0F);
        float to = dynamic.get("to").asFloat(1F);
        boolean exclusive = dynamic.get("exclusive").asBoolean(false);
        return factory.create(from, to, exclusive);
    }

    public interface Factory<T extends RangeContextModifier> {

        T create(float from, float to, boolean exclusive);
    }
}
