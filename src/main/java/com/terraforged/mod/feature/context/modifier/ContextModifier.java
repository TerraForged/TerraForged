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

package com.terraforged.mod.feature.context.modifier;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.feature.context.ChanceContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.function.Function;

public interface ContextModifier {

    Map<String, Function<Dynamic<?>, ContextModifier>> REGISTRY = ImmutableMap.of(
            "elevation", Elevation::deserialize,
            "biome", BiomeModifier::deserialize
    );

    ContextModifier NONE = new None();

    String getName();

    float getChance(BlockPos pos, ChanceContext context);

    <T> Dynamic<T> serialize(DynamicOps<T> ops);

    static ContextModifier parse(String type, Dynamic<?> dynamic) {
        Function<Dynamic<?>, ContextModifier> func = REGISTRY.get(type);
        if (func == null) {
            return NONE;
        }
        return func.apply(dynamic);
    }
}
