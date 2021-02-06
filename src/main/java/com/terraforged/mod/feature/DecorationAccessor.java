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

package com.terraforged.mod.feature;

import com.terraforged.mod.api.feature.decorator.DecorationContext;
import com.terraforged.mod.util.reflect.ReflectUtils;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.feature.WorldDecoratingHelper;

import java.lang.invoke.MethodHandle;

public class DecorationAccessor {

    private static final MethodHandle WORLD = ReflectUtils.field(WorldDecoratingHelper.class, ISeedReader.class);
    private static final MethodHandle GENERATOR = ReflectUtils.field(WorldDecoratingHelper.class, ChunkGenerator.class);

    public static DecorationContext getContext(WorldDecoratingHelper helper) {
        return DecorationContext.of(getWorld(helper), getGenerator(helper));
    }

    public static ISeedReader getWorld(WorldDecoratingHelper helper) {
        try {
            return (ISeedReader) WORLD.invokeExact(helper);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }

    public static ChunkGenerator getGenerator(WorldDecoratingHelper helper) {
        try {
            return (ChunkGenerator) GENERATOR.invokeExact(helper);
        } catch (Throwable t) {
            t.printStackTrace();
            return null;
        }
    }
}
