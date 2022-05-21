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

package com.terraforged.mod.worldgen.cave;

import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class NoiseCaveGenerator {
    protected static final int POOL_SIZE = 32;
    protected static final float DENSITY = 0.05F;
    protected static final float BREACH_THRESHOLD = 0.7F;

    protected final NoiseCave[] caves;
    protected final Module uniqueCaveNoise;
    protected final Module caveBreachNoise;
    protected final ObjectPool<CarverChunk> pool;
    protected final Map<ChunkPos, CarverChunk> cache = new ConcurrentHashMap<>();

    public NoiseCaveGenerator(long seed, RegistryAccess access) {
        var global = access.registryOrThrow(ModRegistry.CAVE.get());
        this.uniqueCaveNoise = createUniqueNoise((int) seed, 500, DENSITY);
        this.caveBreachNoise = createBreachNoise((int) seed + 12, 300, BREACH_THRESHOLD);
        this.caves = global.stream().map(config -> config.withSeed(seed)).toArray(NoiseCave[]::new);
        this.pool = new ObjectPool<>(POOL_SIZE, this::createCarverChunk);
    }

    public NoiseCaveGenerator(long seed, NoiseCaveGenerator other) {
        this.caves = new NoiseCave[other.caves.length];
        this.uniqueCaveNoise = createUniqueNoise((int) seed, 500, DENSITY);
        this.caveBreachNoise = createBreachNoise((int) seed + 12, 300, BREACH_THRESHOLD);
        this.pool = new ObjectPool<>(POOL_SIZE, this::createCarverChunk);
        for (int i = 0; i < caves.length; i++) {
            this.caves[i] = other.caves[i].withSeed(seed);
        }
    }

    public void carve(ChunkAccess chunk, Generator generator) {
        var carver = getPreCarveChunk(chunk);
        carver.terrainData = generator.getChunkData(chunk.getPos());
        carver.mask = caveBreachNoise;

        for (var config : caves) {
            carver.modifier = getModifier(config);

            NoiseCaveCarver.carve(chunk, carver, generator, config, true);
        }
    }

    public void decorate(ChunkAccess chunk, WorldGenLevel region, Generator generator) {
        var carver = getPostCarveChunk(chunk, generator);

        for (var config : caves) {
            NoiseCaveDecorator.decorate(chunk, carver, region, generator, config);
        }

        pool.restore(carver);
    }

    private CarverChunk getPreCarveChunk(ChunkAccess chunk) {
        return cache.computeIfAbsent(chunk.getPos(), p -> pool.take().reset());
    }

    private CarverChunk getPostCarveChunk(ChunkAccess chunk, Generator generator) {
        var carver = cache.remove(chunk.getPos());
        if (carver != null) return carver;

        // Chunk may have been saved in an incomplete state so need run the carve step
        // again to populate the CarverChunk (flag set false to skip setting blocks).

        carver = pool.take().reset();

        carver.mask = caveBreachNoise;
        carver.terrainData = generator.getChunkData(chunk.getPos());

        for (var config : caves) {
            carver.modifier = getModifier(config);

            NoiseCaveCarver.carve(chunk, carver, generator, config, false);
        }

        return carver;
    }

    private Module getModifier(NoiseCave cave) {
        return switch (cave.getType()) {
            case GLOBAL -> Source.ONE;
            case UNIQUE -> uniqueCaveNoise;
        };
    }

    private CarverChunk createCarverChunk() {
        return new CarverChunk(caves.length);
    }

    private static Module createUniqueNoise(int seed, int scale, float density) {
        return new UniqueCaveDistributor(seed + 1286745, 1F / scale, 0.75F, density)
                .clamp(0.2, 1.0).map(0, 1)
                .warp(seed + 781624, 30, 1, 20);
    }

    private static Module createBreachNoise(int seed, int scale, float threshold) {
        return Source.simplexRidge(seed, scale, 2).clamp(threshold * 0.8F, threshold).map(0, 1);
    }
}
