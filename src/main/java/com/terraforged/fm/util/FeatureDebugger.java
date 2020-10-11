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

package com.terraforged.fm.util;

import net.minecraft.world.gen.feature.ConfiguredFeature;
import net.minecraft.world.gen.feature.ConfiguredRandomFeatureList;
import net.minecraft.world.gen.feature.DecoratedFeatureConfig;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.MultipleRandomFeatureConfig;
import net.minecraft.world.gen.feature.SingleRandomFeature;
import net.minecraft.world.gen.feature.TwoFeatureChoiceConfig;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class FeatureDebugger {

    public static List<String> getErrors(ConfiguredFeature<?, ?> feature) {
        List<String> errors = new ArrayList<>();
        checkConfiguredFeature(feature, errors);
        return errors;
    }

    private static void checkConfiguredFeature(ConfiguredFeature<?, ?> feature, List<String> errors) {
        if (!validateConfiguredFeature(feature, errors)) {
            return;
        }

        if (feature.config instanceof DecoratedFeatureConfig) {
            decorated((DecoratedFeatureConfig) feature.config, errors);
            return;
        }

        // note SingleRandomFeature & SingleRandomFeatureConfig names a mixed up
        if (feature.config instanceof SingleRandomFeature) {
            single((SingleRandomFeature) feature.config, errors);
            return;
        }

        if (feature.config instanceof TwoFeatureChoiceConfig) {
            twoChoice((TwoFeatureChoiceConfig) feature.config, errors);
            return;
        }

        if (feature.config instanceof MultipleRandomFeatureConfig) {
            multi((MultipleRandomFeatureConfig) feature.config, errors);
            return;
        }
    }

    private static void decorated(DecoratedFeatureConfig config, List<String> errors) {
        checkConfiguredFeature(config.feature.get(), errors);
        checkDecorator(config.decorator, errors);
    }

    private static void single(SingleRandomFeature config, List<String> errors) {
        for (Supplier<ConfiguredFeature<?, ?>> feature : config.features) {
            checkConfiguredFeature(feature.get(), errors);
        }
    }

    private static void twoChoice(TwoFeatureChoiceConfig config, List<String> errors) {
        checkConfiguredFeature(config.field_227285_a_.get(), errors);
        checkConfiguredFeature(config.field_227286_b_.get(), errors);
    }

    private static void multi(MultipleRandomFeatureConfig config, List<String> errors) {
        for (ConfiguredRandomFeatureList feature : config.features) {
            checkConfiguredFeature(feature.feature.get(), errors);
        }
    }

    private static boolean validateConfiguredFeature(ConfiguredFeature<?, ?> feature, List<String> list) {
        if (feature == null) {
            list.add("null  configured feature - this is bad D:");
            return false;
        }
        return checkFeature(feature.feature, list) && checkConfig(feature.config, list);
    }

    private static boolean checkFeature(Feature<?> feature, List<String> list) {
        if (feature == null) {
            list.add("null feature");
            return false;
        } else if (!ForgeRegistries.FEATURES.containsValue(feature)) {
            list.add("unregistered feature: " + feature.getClass().getName());
            return false;
        }
        return true;
    }

    private static boolean checkConfig(IFeatureConfig config, List<String> list) {
        if (config == null) {
            list.add("null config");
            return false;
        }

        try {
//            config.serialize(JsonOps.INSTANCE);
            return true;
        } catch (Throwable t) {
            list.add("config: " + config.getClass().getName() + ", error: " + t.getMessage());
            return false;
        }
    }

    private static boolean checkDecorator(ConfiguredPlacement<?> decorator, List<String> list) {
        if (decorator == null) {
            list.add("null configured placement");
            return false;
        }

        boolean valid = true;
        // no longer exposed
//        if (decorator.decorator == null) {
//            valid = false;
//            list.add("null placement");
//        } else if (!ForgeRegistries.DECORATORS.containsValue(decorator.decorator)) {
//            list.add("unregistered placement: " + decorator.decorator.getClass().getName());
//        }

        if (decorator.func_242877_b() == null) {
            valid = false;
            list.add("null decorator config");
        } else {
            try {
//                decorator.config.serialize(JsonOps.INSTANCE);
            } catch (Throwable t) {
                valid = false;
                list.add("placement config: " + decorator.func_242877_b().getClass().getName() + ", error: " + t.getMessage());
            }
        }
        return valid;
    }
}
