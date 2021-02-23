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

package com.terraforged.mod.featuremanager.matcher.feature;

import com.google.gson.*;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.mod.featuremanager.modifier.Jsonifiable;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;

import java.util.*;
import java.util.function.Predicate;

public class FeatureMatcher implements Predicate<JsonElement>, Jsonifiable {

    public static final FeatureMatcher ANY = new FeatureMatcher(Collections.emptyList());
    public static final FeatureMatcher NONE = new FeatureMatcher(Collections.emptyList());

    private final List<Rule> rules;

    protected FeatureMatcher(List<Rule> rules) {
        super();
        this.rules = rules;
    }

    @Override
    public String getType() {
        return "match";
    }

    @Override
    public JsonElement toJson(BiomeContext<?> context) {
        JsonArray rules = new JsonArray();
        for (Rule rule : this.rules) {
            rules.add(rule.toJson());
        }
        return rules;
    }

    @Override
    public boolean test(JsonElement element) {
        if (this == ANY) {
            return true;
        }
        if (this == NONE) {
            return false;
        }
        return test(element, createSearch());
    }

    @Override
    public String toString() {
        return "JsonMatcher{" +
                "rules=" + rules +
                '}';
    }

    protected boolean test(JsonElement element, Search search) {
        if (element.isJsonObject()) {
            JsonObject object = element.getAsJsonObject();
            for (Map.Entry<String, JsonElement> e : object.entrySet()) {
                if (testObjectEntry(e.getKey(), e.getValue(), search)) {
                    return search.isComplete();
                }
            }
        } else if (element.isJsonArray()) {
            JsonArray array = element.getAsJsonArray();
            for (int i = 0; i < array.size(); i++) {
                if (testArrayEntry(i, array.get(i), search)) {
                    return search.isComplete();
                }
            }
        } else if (element.isJsonPrimitive()) {
            search.test(element.getAsJsonPrimitive());
            return search.isComplete();
        }
        return search.isComplete();
    }

    protected boolean testObjectEntry(String key, JsonElement element, Search search) {
        if (search.test(key, element)) {
            return search.isComplete();
        }
        return test(element, search);
    }

    protected boolean testArrayEntry(int index, JsonElement element, Search search) {
        return test(element, search);
    }

    protected Search createSearch() {
        return new Search(rules);
    }

    public static Optional<FeatureMatcher> of(JsonElement element) {
        List<Rule> rules = Rule.parseRules(element);
        if (rules.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new FeatureMatcher(rules));
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain the provided arg
     */
    public static FeatureMatcher of(Object arg) {
        return and(arg);
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain ANY of the provided args
     */
    public static FeatureMatcher or(Object... args) {
        if (args.length == 0) {
            return FeatureMatcher.ANY;
        }
        Builder builder = builder();
        for (Object o : args) {
            builder.or(o);
        }
        return builder.build();
    }

    /**
     * Create a FeatureMatcher that matches ConfiguredFeatures that contain ALL of the provided args
     */
    public static FeatureMatcher and(Object... args) {
        if (args.length == 0) {
            return FeatureMatcher.ANY;
        }
        Builder builder = builder();
        for (Object arg : args) {
            builder.and(arg);
        }
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static JsonElement arg(Object arg) {
        if (arg instanceof String) {
            return new JsonPrimitive((String) arg);
        }
        if (arg instanceof Number) {
            return new JsonPrimitive((Number) arg);
        }
        if (arg instanceof Boolean) {
            return new JsonPrimitive((Boolean) arg);
        }
        if (arg instanceof ResourceLocation) {
            return new JsonPrimitive(arg.toString());
        }
        if (arg instanceof ForgeRegistryEntry) {
            return new JsonPrimitive(((ForgeRegistryEntry<?>) arg).getRegistryName() + "");
        }
        return JsonNull.INSTANCE;
    }

    public static class Builder {

        protected List<Rule> rules = Collections.emptyList();
        protected List<JsonPrimitive> values = Collections.emptyList();
        protected Map<String, JsonElement> mappings = Collections.emptyMap();

        public Builder and(Object value) {
            JsonElement element = FeatureMatcher.arg(value);
            if (element.isJsonPrimitive()) {
                and(element.getAsJsonPrimitive());
            }
            return this;
        }

        public Builder and(Boolean value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(Number value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(String value) {
            return and(new JsonPrimitive(value));
        }

        public Builder and(JsonPrimitive value) {
            if (values.isEmpty()) {
                values = new ArrayList<>();
            }
            values.add(value);
            return this;
        }

        public Builder and(String key, JsonElement value) {
            if (mappings.isEmpty()) {
                mappings = new HashMap<>();
            }
            mappings.put(key, value);
            return this;
        }

        public Builder or(Collection<Object> collection) {
            for (Object o : collection) {
                or(o);
            }
            return this;
        }

        public Builder or(Object value) {
            JsonElement element = FeatureMatcher.arg(value);
            if (element.isJsonPrimitive()) {
                or(element.getAsJsonPrimitive());
            }
            return this;
        }

        public Builder or(Boolean value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(Number value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(String value) {
            return or(new JsonPrimitive(value));
        }

        public Builder or(JsonPrimitive value) {
            return newRule().and(value);
        }

        public Builder or(String key, JsonElement value) {
            return newRule().and(key, value);
        }

        public Builder newRule() {
            if (!values.isEmpty() || !mappings.isEmpty()) {
                if (rules.isEmpty()) {
                    rules = new ArrayList<>();
                }
                rules.add(new Rule(values, mappings));
                values = Collections.emptyList();
                mappings = Collections.emptyMap();
            }
            return this;
        }

        public FeatureMatcher build() {
            if (rules.isEmpty() && values.isEmpty()) {
                return FeatureMatcher.NONE;
            }
            newRule();
            return new FeatureMatcher(rules);
        }
    }
}
