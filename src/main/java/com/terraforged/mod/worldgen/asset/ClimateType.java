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

package com.terraforged.mod.worldgen.asset;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.codec.LazyCodec;
import it.unimi.dsi.fastutil.objects.Object2FloatMap;
import it.unimi.dsi.fastutil.objects.Object2FloatOpenHashMap;
import net.minecraft.resources.ResourceLocation;

import java.util.LinkedHashMap;

public class ClimateType {
    public static final String IGNORE = "forge:registry_name";

    public static final Codec<ClimateType> CODEC = LazyCodec.of(() -> new Codec<>() {
        @Override
        public <T> DataResult<Pair<ClimateType, T>> decode(DynamicOps<T> ops, T input) {
            return ops.getMap(input).map(map -> {
                var weights = new Object2FloatOpenHashMap<ResourceLocation>();
                map.entries().forEach(e -> {
                    var name = ops.getStringValue(e.getFirst()).result().orElseThrow();
                    if (name.equals(IGNORE)) return;

                    float weight = ops.getNumberValue(e.getSecond()).result().orElseThrow().floatValue();
                    weights.put(new ResourceLocation(name), weight);
                });
                return new ClimateType(weights);
            }).map(weights -> Pair.of(weights, input));
        }

        @Override
        public <T> DataResult<T> encode(ClimateType input, DynamicOps<T> ops, T prefix) {
            var map = new LinkedHashMap<T, T>();
            for (var entry : input.weights.object2FloatEntrySet()) {
                map.put(ops.createString(entry.getKey().toString()), ops.createFloat(entry.getFloatValue()));
            }
            return DataResult.success(ops.createMap(map));
        }
    });

    private final Object2FloatMap<ResourceLocation> weights;

    public ClimateType(Object2FloatMap<ResourceLocation> weights) {
        this.weights = weights;
    }

    public Object2FloatMap<ResourceLocation> getWeights() {
        return weights;
    }
}
