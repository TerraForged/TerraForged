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

package com.terraforged.mod.featuremanager.modifier;

import com.terraforged.mod.featuremanager.matcher.BiomeFeatureMatcher;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;

public class Modifier<T> implements Comparable<Modifier<T>> {

    private final BiomeFeatureMatcher matcher;
    private final T modifier;

    public Modifier(BiomeMatcher biomeMatcher, FeatureMatcher featureMatcher, T modifier) {
        this(new BiomeFeatureMatcher(biomeMatcher, featureMatcher), modifier);
    }

    public Modifier(BiomeFeatureMatcher matcher, T modifier) {
        this.matcher = matcher;
        this.modifier = modifier;
    }

    public BiomeFeatureMatcher getMatcher() {
        return matcher;
    }

    public T getModifier() {
        return modifier;
    }

    @Override
    public int compareTo(Modifier<T> o) {
        return matcher.compareTo(o.matcher);
    }
}
