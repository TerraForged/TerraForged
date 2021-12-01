package com.terraforged.mod.worldgen;

import com.terraforged.mod.util.Timer;
import com.terraforged.mod.worldgen.terrain.TerrainData;
import com.terraforged.mod.worldgen.terrain.TerrainGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.BiFunction;

public class GeneratorCache {
    private final TerrainGenerator generator;
    private final Timer noiseTimer = new Timer("NOISE_GEN", 10, TimeUnit.SECONDS);
    private final Map<ChunkPos, ForkJoinTask<TerrainData>> cache = new ConcurrentHashMap<>();

    public GeneratorCache(TerrainGenerator generator) {
        this.generator = generator;
    }

    public void drop(ChunkPos pos) {
        var task = cache.remove(pos);
        if (task == null || task.isDone()) return;

        generator.restore(task.join());
    }

    public void hint(ChunkPos pos) {
        getAsync(pos);
    }

    public CompletableFuture<ChunkAccess> hint(ChunkAccess chunk) {
        hint(chunk.getPos());
        return CompletableFuture.completedFuture(chunk);
    }

    public TerrainData getNow(ChunkPos pos) {
        return getAsync(pos).join();
    }

    @Nullable
    public TerrainData getIfReady(ChunkPos pos) {
        var task = cache.get(pos);
        if (task == null || !task.isDone()) return null;

        return task.join();
    }

    public ForkJoinTask<TerrainData> getAsync(ChunkPos pos) {
        return cache.computeIfAbsent(pos, this::compute);
    }

    public <T> CompletableFuture<ChunkAccess> combineAsync(Executor executor,
                                                           ChunkAccess chunkAccess,
                                                           BiFunction<ChunkAccess, TerrainData, ChunkAccess> function) {

        return CompletableFuture.completedFuture(chunkAccess)
                .thenApplyAsync(chunk -> function.apply(chunk, getNow(chunk.getPos())), executor);
    }

    protected ForkJoinTask<TerrainData> compute(ChunkPos pos) {
        return ForkJoinPool.commonPool().submit(() -> generate(pos));
    }

    protected TerrainData generate(ChunkPos pos) {
        try (var timer = noiseTimer.start()) {
            return generator.generate(pos);
        }
    }
}
