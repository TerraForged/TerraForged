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

package com.terraforged.mod.featuremanager;

import com.google.gson.JsonElement;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import io.netty.handler.codec.EncoderException;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Optional;

public class FeatureSerializer {

    public static final Marker MARKER = MarkerManager.getMarker("Serializer");

    public static JsonElement serialize(ConfiguredFeature<?, ?> feature) {
        return Codecs.encode(ConfiguredFeature.field_242763_a, feature);
    }

    public static ConfiguredFeature<?, ?> deserializeUnchecked(JsonElement element) {
        return Codecs.decode(ConfiguredFeature.field_242763_a, element).orElseThrow(EncoderException::new);
    }

    public static Optional<ConfiguredFeature<?, ?>> deserialize(JsonElement element) {
        try {
            return Optional.of(deserializeUnchecked(element));
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static <T> T encode(ConfiguredFeature<?, ?> feature, DynamicOps<T> ops) {
        return Codecs.encodeAndGet(ConfiguredFeature.field_242763_a, feature, ops);
    }

    public static <T> Optional<ConfiguredFeature<?, ?>> decode(OptionalDynamic<T> dynamic) {
        return dynamic.get().result().flatMap(FeatureSerializer::decode);
    }

    public static <T> Optional<ConfiguredFeature<?, ?>> decode(Dynamic<T> dynamic) {
        return Codecs.decode(ConfiguredFeature.field_242763_a, dynamic.getValue(), dynamic.getOps());
    }
}
