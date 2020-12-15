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

package com.terraforged.api.feature.decorator;

import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.FieldAccessor;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

import java.lang.reflect.Modifier;

class DecorationHelperFallback {

    private static final FieldAccessor<WorldDecoratingHelper, ISeedReader> REGION = Accessor.field(
            WorldDecoratingHelper.class,
            ISeedReader.class,
            Accessor.fieldModifiers(Modifier::isPrivate, Modifier::isFinal)
    );

    private static final FieldAccessor<WorldDecoratingHelper, ChunkGenerator> GENERATOR = Accessor.field(
            WorldDecoratingHelper.class,
            ChunkGenerator.class,
            Accessor.fieldModifiers(Modifier::isPrivate, Modifier::isFinal)
    );

    static DecorationContext getContext(WorldDecoratingHelper helper) {
        try {
            ISeedReader region = REGION.get(helper);
            ChunkGenerator generator = GENERATOR.get(helper);
            return DecorationContext.of(region, generator);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException("Unable to access context info for " + helper);
        }
    }
}
