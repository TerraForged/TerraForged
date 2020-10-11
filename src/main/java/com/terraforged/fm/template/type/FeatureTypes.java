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

package com.terraforged.fm.template.type;

import com.terraforged.fm.template.decorator.Decorator;
import com.terraforged.fm.template.decorator.NoDecoratorFactory;
import com.terraforged.fm.template.feature.Placement;
import com.terraforged.fm.template.type.tree.TreeDecoratorFactory;
import com.terraforged.fm.template.type.tree.TreePlacement;

import java.util.HashMap;
import java.util.Map;

public class FeatureTypes {

    private static final Map<String, FeatureType> types = new HashMap<>();
    public static final FeatureType ANY = register("any", Placement.ANY, new NoDecoratorFactory());
    public static final FeatureType TREE = register("tree", TreePlacement.PLACEMENT, new TreeDecoratorFactory());

    public static FeatureType getType(String type) {
        return types.getOrDefault(type, ANY);
    }

    public static synchronized void clearFeatures() {
        types.values().forEach(FeatureType::clear);
    }

    private static FeatureType register(String name, Placement placement, Decorator.Factory<?> factory) {
        return register(new FeatureType(name, placement, factory));
    }

    private static FeatureType register(FeatureType type) {
        types.put(type.getName(), type);
        return type;
    }
}
