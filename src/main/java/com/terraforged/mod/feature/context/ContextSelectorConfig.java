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

package com.terraforged.mod.feature.context;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.List;

public class ContextSelectorConfig implements IFeatureConfig {

    public static final Codec<ContextSelectorConfig> CODEC = Codecs.create(
            ContextSelectorConfig::serialize,
            ContextSelectorConfig::deserialize
    );

    public final List<ContextualFeature> features;

    public ContextSelectorConfig(List<ContextualFeature> features) {
        this.features = features;
    }

    public static <T> Dynamic<T> serialize(ContextSelectorConfig config, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("features"), Codecs.createList(ContextualFeature.CODEC, ops, config.features)
        )));
    }

    public static <T> ContextSelectorConfig deserialize(Dynamic<T> dynamic) {
        return new ContextSelectorConfig(dynamic.get("features").asList(ContextualFeature::deserialize));
    }
}
