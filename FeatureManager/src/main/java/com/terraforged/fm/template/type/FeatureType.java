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

import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.template.decorator.Decorator;
import com.terraforged.fm.template.feature.Placement;
import net.minecraft.util.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class FeatureType {

    private final String name;
    private final Placement placement;
    private final Decorator.Factory<?> decorator;
    private final List<ResourceLocation> templates = new ArrayList<>();

    public FeatureType(String name, Placement placement, Decorator.Factory<?> decorator) {
        this.name = name;
        this.placement = placement;
        this.decorator = decorator;
    }

    public String getName() {
        return name;
    }

    public Placement getPlacement() {
        return placement;
    }

    public Decorator.Factory<?> getDecorator() {
        return decorator;
    }

    public synchronized void register(ResourceLocation template) {
        templates.add(template);
    }

    public synchronized void clear() {
        templates.clear();
    }

    @Override
    public String toString() {
        return name;
    }

    public FeatureMatcher matcher() {
        FeatureMatcher.Builder builder = FeatureMatcher.builder();
        for (ResourceLocation template : templates) {
            builder.or(template);
        }
        return builder.build();
    }
}
