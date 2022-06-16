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

import com.terraforged.mod.util.ReflectionUtil;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.lang.invoke.MethodHandle;
import java.util.concurrent.atomic.AtomicInteger;

public class DimensionEffects extends DimensionSpecialEffects {
    public static final AtomicInteger CLOUD_HEIGHT = new AtomicInteger(300);

    private static final MethodHandle REGISTRY_GETTER = ReflectionUtil.field(DimensionSpecialEffects.class, Object2ObjectMap.class);

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
            getRegistry().put(location, effects);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    private static Object2ObjectMap<ResourceLocation, DimensionSpecialEffects> getRegistry() {
        try {
            return (Object2ObjectMap<ResourceLocation, DimensionSpecialEffects>) REGISTRY_GETTER.invokeExact();
        } catch (Throwable e) {
            throw new Error(e);
        }
    }
}
