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

package com.terraforged.mod.featuremanager.util.identity;

import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.terraforged.mod.featuremanager.FeatureSerializer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.world.gen.feature.ConfiguredFeature;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class FeatureIdentity implements Identity {

    public static final FeatureIdentity NONE = new FeatureIdentity(Collections.emptyList());

    private final List<String> identity;
    private final Supplier<String> ownerString;
    private final Supplier<String> identityString;

    public FeatureIdentity(List<String> identity) {
        this.identity = identity;

        this.ownerString = Suppliers.memoize(() -> {
            StringBuilder sb = new StringBuilder(32);
            sb.append("Owners[");
            for (int i = 0; i < identity.size(); i++) {
                String id = identity.get(i);
                int namespaceEnd = id.indexOf(':');
                sb.append(i > 0 ? "," : "").append(id, 0, namespaceEnd);
            }
            sb.append("]");
            return sb.toString();
        });

        this.identityString = Suppliers.memoize(() -> {
            StringBuilder sb = new StringBuilder(32);
            sb.append("Identities[");
            for (int i = 0; i < identity.size(); i++) {
                sb.append(i > 0 ? "," : "").append(identity.get(i));
            }
            sb.append(']');
            return sb.toString();
        });
    }

    public String getOwners() {
        return ownerString.get();
    }

    @Override
    public String getIdentity() {
        return identityString.get();
    }

    @Override
    public String toString() {
        return "FeatureIdentity{" +
                "identity=" + identity +
                '}';
    }

    public static FeatureIdentity getIdentity(ConfiguredFeature<?, ?> feature) {
        try {
            JsonElement jsonElement = FeatureSerializer.serialize(feature);
            return getIdentity(feature, jsonElement);
        } catch (Throwable t) {
            return FeatureIdentity.NONE;
        }
    }

    public static FeatureIdentity getIdentity(ConfiguredFeature<?, ?> feature, JsonElement jsonElement) {
        ResourceLocation name = WorldGenRegistries.CONFIGURED_FEATURE.getKey(feature);
        if (name != null) {
            return new FeatureIdentity(Collections.singletonList(name.toString()));
        }
        try {
            List<String> list = new ArrayList<>(4);
            collectIdentifiers(jsonElement, list);
            if (list.size() == 0) {
                return FeatureIdentity.NONE;
            }
            Collections.sort(list);
            return new FeatureIdentity(list);
        } catch (Throwable t) {
            return FeatureIdentity.NONE;
        }
    }

    public static void collectIdentifiers(JsonElement element, List<String> collector) {
        if (element.isJsonObject()) {
            for (Map.Entry<String, JsonElement> entry : element.getAsJsonObject().entrySet()) {
                collectIdentifier(entry.getKey(), collector);
                collectIdentifiers(entry.getValue(), collector);
            }
        } else if (element.isJsonArray()) {
            for (JsonElement entry : element.getAsJsonArray()) {
                collectIdentifiers(entry, collector);
            }
        } else if (element.isJsonPrimitive()) {
            collectIdentifier(element.getAsString(), collector);
        }
    }

    private static void collectIdentifier(String string, List<String> collector) {
        if (string.indexOf(':') != -1 && !collector.contains(string)) {
            collector.add(string);
        }
    }
}
