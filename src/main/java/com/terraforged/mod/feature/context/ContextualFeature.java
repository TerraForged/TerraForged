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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.terraforged.fm.FeatureSerializer;
import com.terraforged.fm.util.codec.CodecException;
import com.terraforged.fm.util.codec.Codecs;
import com.terraforged.mod.feature.context.modifier.ContextModifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContextualFeature {

    public static final Codec<ContextualFeature> CODEC = Codecs.create(
            ContextualFeature::serialize,
            ContextualFeature::deserialize
    );

    public final float chance;
    public final List<ContextModifier> contexts;
    public final ConfiguredFeature<?, ?> feature;

    public ContextualFeature(ConfiguredFeature<?, ?> feature, float chance, List<ContextModifier> contexts) {
        this.chance = chance;
        this.feature = feature;
        this.contexts = contexts;
    }

    public String getName() {
        return "chance";
    }

    public float getChance(BlockPos pos, ChanceContext context) {
        float chance = this.chance;
        for (ContextModifier modifier : contexts) {
            chance *= modifier.getChance(pos, context);
        }
        return chance;
    }

    public static <T> Dynamic<T> serialize(ContextualFeature feature, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("feature"), FeatureSerializer.encode(feature.feature, ops),
                ops.createString("context"), serializeContext(feature, ops).getValue()
        )));
    }

    private static <T> Dynamic<T> serializeContext(ContextualFeature feature, DynamicOps<T> ops) {
        Map<T, T> map = new LinkedHashMap<>();
        map.put(ops.createString("chance"), ops.createFloat(feature.chance));
        for (ContextModifier context : feature.contexts) {
            map.put(ops.createString(context.getName()), context.serialize(ops).getValue());
        }
        return new Dynamic<>(ops, ops.createMap(map));
    }

    public static <T> ContextualFeature deserialize(Dynamic<T> dynamic) {
        ConfiguredFeature<?, ?> feature = FeatureSerializer.decode(dynamic.get("feature")).orElseThrow(CodecException.get("Failed to deserialize ContextualFeature"));
        OptionalDynamic<?> context = dynamic.get("context");
        float chance = context.get("chance").asFloat(0);
        List<ContextModifier> contexts = deserializeContexts(context);
        return new ContextualFeature(feature, chance, contexts);
    }

    private static List<ContextModifier> deserializeContexts(OptionalDynamic<?> dynamic) {
        return Codecs.getResult(dynamic.flatMap(d -> d.getMapValues().map(map -> {
            List<ContextModifier> contexts = new ArrayList<>(map.size());
            map.forEach((key, value) -> {
                String name = key.asString("");
                if (!name.equals("chance")) {
                    contexts.add(ContextModifier.parse(name, value));
                }
            });
            return contexts;
        }))).orElseThrow(CodecException.get("Failed to deserialize Contexts"));
    }
}
