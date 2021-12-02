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

package com.terraforged.mod.client.ingame;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

public class DimensionEffects extends DimensionSpecialEffects {
    public static final AtomicInteger CLOUD_HEIGHT = new AtomicInteger(300);

    protected final DimensionSpecialEffects source;

    public DimensionEffects() {
        this(new OverworldEffects());
    }

    public DimensionEffects(DimensionSpecialEffects source) {
        super(source.getCloudHeight(), source.hasGround(), source.skyType(), source.forceBrightLightmap(), source.constantAmbientLight());
        this.source = source;
    }

    @Override
    public float getCloudHeight() {
        return CLOUD_HEIGHT.get();
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 pos, float intensity) {
        return source.getBrightnessDependentFogColor(pos, intensity);
    }

    @Override
    public boolean isFoggyAt(int x, int z) {
        return source.isFoggyAt(x, z);
    }

    public static void register(ResourceLocation location, DimensionSpecialEffects effects) {
        try {
            var getter = REGISTRY_GETTER.get();
            var map = (Map) getter.get(null);
            map.put(location, effects);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private static final Supplier<Field> REGISTRY_GETTER = Suppliers.memoize(() -> {
        for (var field : DimensionSpecialEffects.class.getDeclaredFields()) {
            if (Modifier.isStatic(field.getModifiers()) && field.getType() == Object2ObjectMap.class) {
                field.setAccessible(true);
                return field;
            }
        }
        throw new Error("Could reflect DimensionSpecialEffects map!");
    });
}
