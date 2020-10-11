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

package com.terraforged.api.biome.surface;

import com.terraforged.api.biome.surface.builder.Delegate;
import com.terraforged.fm.GameContext;
import net.minecraft.util.RegistryKey;
import net.minecraft.world.biome.Biome;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class SurfaceManager {

    private final GameContext context;
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<Biome, Surface> surfaces = new HashMap<>();

    public SurfaceManager(GameContext context) {
        this.context = context;
    }

    public Surface getSurface(Biome biome) {
        lock.readLock().lock();
        Surface surface = surfaces.get(biome);
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

    public final SurfaceManager replace(RegistryKey<Biome> biome, Surface surface) {
        return replace(context.biomes.get(biome), surface);
    }

    public final SurfaceManager replace(Biome biome, Surface surface) {
        lock.writeLock().lock();
        surfaces.put(biome, surface);
        lock.writeLock().unlock();
        return this;
    }

    @SafeVarargs
    public final SurfaceManager replace(Surface surface, RegistryKey<Biome>... biomes) {
        for (RegistryKey<Biome> biome : biomes) {
            replace(biome, surface);
        }
        return this;
    }

    public final SurfaceManager replace(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            replace(biome, surface);
        }
        return this;
    }

    public final SurfaceManager prepend(Biome biome, Surface surface) {
        Surface result = surface.then(getOrCreateSurface(biome));
        return replace(biome, result);
    }

    public final SurfaceManager prepend(RegistryKey<Biome> biome, Surface surface) {
        return prepend(context.biomes.get(biome), surface);
    }

    public final SurfaceManager prepend(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            prepend(biome, surface);
        }
        return this;
    }

    @SafeVarargs
    public final SurfaceManager prepend(Surface surface, RegistryKey<Biome>... biomes) {
        for (RegistryKey<Biome> biome : biomes) {
            prepend(biome, surface);
        }
        return this;
    }

    public final SurfaceManager append(Biome biome, Surface surface) {
        Surface result = getOrCreateSurface(biome).then(surface);
        return replace(biome, result);
    }

    public final SurfaceManager append(RegistryKey<Biome> biome, Surface surface) {
        return append(context.biomes.get(biome), surface);
    }

    public final SurfaceManager append(Surface surface, Biome... biomes) {
        for (Biome biome : biomes) {
            append(biome, surface);
        }
        return this;
    }

    @SafeVarargs
    public final SurfaceManager append(Surface surface, RegistryKey<Biome>... biomes) {
        for (RegistryKey<Biome> biome : biomes) {
            append(biome, surface);
        }
        return this;
    }

    public Surface getSurface(SurfaceContext context) {
        if (context.biome == context.cached.biome && context.cached.surface != null) {
            return context.cached.surface;
        }
        context.cached.biome = context.biome;
        context.cached.surface = getOrCreateSurface(context.biome);
        return context.cached.surface;
    }
}
