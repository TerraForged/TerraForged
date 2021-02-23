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

package com.terraforged.mod.featuremanager;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import com.terraforged.mod.featuremanager.util.codec.CodecException;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.MarkerManager;

import java.util.Optional;

public class FeatureSerializer {

    public static final Marker MARKER = MarkerManager.getMarker("Serializer");

    public static JsonElement serialize(ConfiguredFeature<?, ?> feature) {
        Optional<JsonElement> registered = Codecs.encodeOpt(ConfiguredFeature.field_242763_a, feature);
        if (registered.isPresent()) {
            return registered.get();
        }

        Optional<JsonElement> unregistered = serializeUnregistered(feature);
        if (unregistered.isPresent()) {
            return unregistered.get();
        }

        ResourceLocation name = WorldGenRegistries.CONFIGURED_FEATURE.getKey(feature);
        if (name != null) {
            return new JsonPrimitive(name.toString());
        }

        throw CodecException.of("Failed to serialize feature: {}", feature);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Optional<JsonElement> serializeUnregistered(ConfiguredFeature feature) {
        try {
            return Codecs.encodeOpt(feature.feature.getCodec(), feature);
        } catch (Throwable t) {
            return Optional.empty();
        }
    }

    public static ConfiguredFeature<?, ?> deserializeUnchecked(JsonElement element) {
        final Optional<ConfiguredFeature<?, ?>> feature;

        if (element.isJsonPrimitive()) {
            String string = element.getAsString();
            ResourceLocation name = ResourceLocation.tryCreate(string);
            if (name == null) {
                throw CodecException.of("Failed to deserialize feature. Invalid registry name: {}", element);
            }
            feature = WorldGenRegistries.CONFIGURED_FEATURE.getOptional(name);
        } else {
            feature = Codecs.decode(ConfiguredFeature.field_242763_a, element);
        }

        return feature.orElseThrow(CodecException.get("Failed to deserialize feature Json: {}", element));
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
