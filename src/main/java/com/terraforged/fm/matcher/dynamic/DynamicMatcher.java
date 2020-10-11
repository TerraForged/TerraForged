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

package com.terraforged.fm.matcher.dynamic;

import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredRandomFeatureList;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class DynamicMatcher implements Predicate<ConfiguredFeature<?, ?>> {

    public static final DynamicMatcher NONE = DynamicMatcher.of(f -> false);

    private final Predicate<ConfiguredFeature<?, ?>> predicate;

    private DynamicMatcher(Predicate<ConfiguredFeature<?, ?>> predicate) {
        this.predicate = predicate;
    }

    @Override
    public boolean test(ConfiguredFeature<?, ?> feature) {
        if (feature.config instanceof DecoratedFeatureConfig) {
            return decorated((DecoratedFeatureConfig) feature.config);
        }

        // note SingleRandomFeature & SingleRandomFeatureConfig names a mixed up
        if (feature.config instanceof SingleRandomFeature) {
            return single((SingleRandomFeature) feature.config);
        }

        if (feature.config instanceof TwoFeatureChoiceConfig) {
            return twoChoice((TwoFeatureChoiceConfig) feature.config);
        }

        if (feature.config instanceof MultipleRandomFeatureConfig) {
            return multi((MultipleRandomFeatureConfig) feature.config);
        }

        return predicate.test(feature);
    }

    private boolean decorated(DecoratedFeatureConfig config) {
        return test(config.feature.get());
    }

    private boolean single(SingleRandomFeature config) {
        for (Supplier<ConfiguredFeature<?, ?>> feature : config.features) {
            if (test(feature.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean twoChoice(TwoFeatureChoiceConfig config) {
        if (test(config.field_227285_a_.get())) {
            return true;
        }
        return test(config.field_227286_b_.get());
    }

    private boolean multi(MultipleRandomFeatureConfig config) {
        for (ConfiguredRandomFeatureList feature : config.features) {
            if (test(feature.feature.get())) {
                return true;
            }
        }
        return false;
    }

    public static DynamicMatcher of(Predicate<ConfiguredFeature<?, ?>> predicate) {
        return new DynamicMatcher(predicate);
    }

    public static DynamicMatcher feature(Predicate<Feature<?>> predicate) {
        return DynamicMatcher.of(f -> predicate.test(f.feature));
    }

    public static DynamicMatcher feature(Feature<?> feature) {
        return DynamicMatcher.feature(f -> f == feature);
    }

    public static DynamicMatcher feature(Class<? extends Feature> type) {
        return DynamicMatcher.feature(type::isInstance);
    }

    public static DynamicMatcher config(Predicate<IFeatureConfig> predicate) {
        return DynamicMatcher.of(f -> predicate.test(f.config));
    }

    public static DynamicMatcher config(IFeatureConfig config) {
        return DynamicMatcher.of(f -> f.config == config);
    }

    public static DynamicMatcher config(Class<? extends IFeatureConfig> type) {
        return DynamicMatcher.config(type::isInstance);
    }
}
