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

package com.terraforged.mod.data.gen.feature;

import com.terraforged.fm.data.FeatureInjectorProvider;
import com.terraforged.fm.matcher.feature.FeatureMatcher;
import com.terraforged.fm.transformer.FeatureTransformer;
import com.terraforged.mod.feature.feature.DiskFeature;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.feature.Feature;

public class Sediments {

    public static void addInjectors(FeatureInjectorProvider provider) {
        provider.add("sediments/clay", FeatureMatcher.and(Blocks.CLAY, Feature.DISK), FeatureTransformer.replace(Feature.DISK,  DiskFeature.INSTANCE));
        provider.add("sediments/dirt", FeatureMatcher.and(Blocks.DIRT, Feature.DISK), FeatureTransformer.replace(Feature.DISK, DiskFeature.INSTANCE));
        provider.add("sediments/gravel", FeatureMatcher.and(Blocks.GRAVEL, Feature.DISK), FeatureTransformer.replace(Feature.DISK, DiskFeature.INSTANCE));
    }
}
