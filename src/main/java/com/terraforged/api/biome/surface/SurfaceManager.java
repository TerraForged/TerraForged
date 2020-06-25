/*
 *
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

package com.terraforged.api.biome.surface;

import com.terraforged.api.biome.surface.builder.Delegate;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SurfaceManager {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<ResourceLocation, Surface> surfaces = new HashMap<>();

    public Surface getSurface(Biome biome) {
        lock.readLock().lock();
        Surface surface = surfaces.get(biome.getRegistryName());
        lock.readLock().unlock();
        return surface;
    }

    public Surface getOrCreateSurface(Biome biome) {
        Surface surface = getSurface(biome);
        if (surface == null) {
            surface = Delegate.FUNC.apply(biome);
            replace(biome, surface);
        }
        return surface;
    }

    public SurfaceManager replace(Biome biome, Surface surface) {
        lock.writeLock().lock();
        surfaces.put(biome.getRegistryName(), surface);
        lock.writeLock().unlock();
        return this;
    }

    public SurfaceManager replace(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            replace(biome, surface);
        }
        return this;
    }

    public SurfaceManager prepend(Biome biome, Surface surface) {
        Surface result = surface.then(getOrCreateSurface(biome));
        return replace(biome, result);
    }

    public SurfaceManager prepend(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            prepend(biome, surface);
        }
        return this;
    }

    public SurfaceManager append(Biome biome, Surface surface) {
        Surface result = getOrCreateSurface(biome).then(surface);
        return replace(biome, result);
    }

    public SurfaceManager append(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            append(biome, surface);
        }
        return this;
    }

    public Surface getSurface(SurfaceContext context) {
        if (context.biome == context.cached.biome) {
            return context.cached.surface;
        }
        context.cached.biome = context.biome;
        context.cached.surface = getOrCreateSurface(context.biome);
        return context.cached.surface;
    }
}
