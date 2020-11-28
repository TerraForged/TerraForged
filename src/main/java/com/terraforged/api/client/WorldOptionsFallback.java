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

package com.terraforged.api.client;

import com.terraforged.mod.util.reflect.Accessor;
import com.terraforged.mod.util.reflect.MethodAccessor;
import net.minecraft.client.gui.screen.WorldOptionsScreen;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

public class WorldOptionsFallback {

    private static final MethodAccessor<WorldOptionsScreen> ACCESSOR = Accessor.method(
            WorldOptionsScreen.class,
            Accessor.methodModifiers(Modifier::isProtected).and(Accessor.paramTypes(DimensionGeneratorSettings.class))
    );

    static void update(WorldOptionsScreen screen, DimensionGeneratorSettings settings) {
        try {
            ACCESSOR.invokeVoid(screen, settings);
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
