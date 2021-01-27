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

package com.terraforged.mod.biome.transformer;

import com.google.gson.JsonElement;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.featuremanager.data.DataManager;
import net.minecraft.util.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class BiomeTransformerCache {

    public static final String BIOMES_PATH = "worldgen/biome";
    private static final long TIMEOUT = TimeUnit.SECONDS.toMillis(60);
    private static final BiomeTransformerCache INSTANCE = new BiomeTransformerCache();

    private final AtomicLong timestamp = new AtomicLong();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<ResourceLocation, JsonElement> biomes = new HashMap<>();

    public JsonElement get(ResourceLocation location) {
        lock.readLock().lock();
        try {
            return biomes.get(location);
        } finally {
            lock.readLock().unlock();
        }
    }

    private BiomeTransformerCache get() {
        long now = System.currentTimeMillis();
        long then = timestamp.get();
        if (now - then > TIMEOUT && timestamp.compareAndSet(then, now)) {
            reload();
        }
        return this;
    }

    private void reload() {
        lock.writeLock().lock();
        try {
            biomes.clear();
            try (DataManager dataManager = DataManager.of(TerraForgedMod.DATAPACK_DIR)) {
                dataManager.forEachJson(BIOMES_PATH, (location, json) -> {
                    ResourceLocation name = toRegistryName(location);
                    biomes.put(name, json);
                });
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private ResourceLocation toRegistryName(ResourceLocation location) {
        String namespace = location.getNamespace();
        String path = location.getPath();
        int fileStart = path.lastIndexOf('/') + 1;
        int fileEnd = path.lastIndexOf('.');
        String name = path.substring(fileStart, fileEnd);
        return new ResourceLocation(namespace, name);
    }

    public static BiomeTransformerCache getInstance() {
        return INSTANCE.get();
    }
}
