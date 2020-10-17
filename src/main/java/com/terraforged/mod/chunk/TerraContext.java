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

package com.terraforged.mod.chunk;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.api.biome.surface.ChunkSurfaceBuffer;
import com.terraforged.api.biome.surface.SurfaceContext;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.core.concurrent.task.LazySupplier;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.core.tile.gen.TileGenerator;
import com.terraforged.fm.GameContext;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.config.PerfDefaults;
import com.terraforged.mod.material.Materials;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.WorldGeneratorFactory;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.DimensionSettings;

import java.util.function.Supplier;

public class TerraContext extends GeneratorContext {

    public final long worldSeed;
    public final LazySupplier<Heightmap> heightmap;
    public final TerraSettings terraSettings;
    public final LazySupplier<Materials> materials = LazySupplier.of(Materials::create);

    public final GameContext gameContext;

    public TerraContext(TerraContext other, GameContext gameContext) {
        super(other.terrain, other.settings, other.terrainFactory, TerraContext::createCache);
        this.worldSeed = other.worldSeed;
        this.terraSettings = other.terraSettings;
        this.gameContext = gameContext;
        this.heightmap = factory.then(WorldGeneratorFactory::getHeightmap);
    }

    public TerraContext(TerraSettings settings, GameContext gameContext) {
        super(Terrains.create(settings), settings, TerraTerrainProvider::new, TerraContext::createCache);
        this.worldSeed = settings.world.seed;
        this.gameContext = gameContext;
        this.terraSettings = settings;
        this.heightmap = factory.then(WorldGeneratorFactory::getHeightmap);
    }

    public TerraContext(Terrains terrain, TerraSettings settings, GameContext gameContext) {
        super(terrain, settings, TerraTerrainProvider::new, TerraContext::createCache);
        this.worldSeed = settings.world.seed;
        this.gameContext = gameContext;
        this.terraSettings = settings;
        this.heightmap = factory.then(WorldGeneratorFactory::getHeightmap);
    }

    public DecoratorContext decorator(IChunk chunk) {
        return new DecoratorContext(chunk, levels, terrain, factory.get().getClimate(), false);
    }

    public SurfaceContext surface(ChunkSurfaceBuffer buffer, TerraContainer biomes, DimensionSettings settings) {
        return new SurfaceContext(buffer, biomes, levels, terrain, factory.get().getClimate(), settings, seed.get());
    }

    public static TileCache createCache(WorldGeneratorFactory factory) {
        Log.info("CREATING CACHE...");
        CommentedConfig config = PerfDefaults.getAndPrintPerfSettings();
        boolean batching = config.getOrElse("batching", false);
        int tileSize = Math.min(PerfDefaults.MAX_TILE_SIZE, Math.max(2, config.getInt("tile_size")));
        int batchCount = Math.min(PerfDefaults.MAX_BATCH_COUNT, Math.max(1, config.getInt("batch_count")));
        int threadCount = Math.min(PerfDefaults.MAX_THREAD_COUNT, Math.max(1, config.getInt("thread_count")));
        return TileGenerator.builder()
                .pool(ThreadPools.create(threadCount, batching))
                .size(tileSize, 2)
                .batch(batchCount)
                .factory(factory)
                .build()
                .toCache();
    }
}
