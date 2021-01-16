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
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.concurrent.thread.ThreadPools;
import com.terraforged.engine.tile.gen.TileCache;
import com.terraforged.engine.tile.gen.TileGenerator;
import com.terraforged.engine.world.GeneratorContext;
import com.terraforged.engine.world.WorldGeneratorFactory;
import com.terraforged.engine.world.heightmap.Heightmap;
import com.terraforged.mod.Log;
import com.terraforged.mod.api.biome.surface.SurfaceChunk;
import com.terraforged.mod.api.biome.surface.SurfaceContext;
import com.terraforged.mod.api.chunk.column.DecoratorContext;
import com.terraforged.mod.biome.TFBiomeContainer;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.config.PerfDefaults;
import com.terraforged.mod.material.Materials;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.DimensionSettings;

public class TerraContext extends GeneratorContext {

    public final long worldSeed;
    public final LazySupplier<Heightmap> heightmap;
    public final TerraSettings terraSettings;
    public final LazySupplier<Materials> materials = LazySupplier.supplied(this::getTerraSettings, Materials::create);

    public final TFBiomeContext biomeContext;

    public TerraContext(TerraContext other, TFBiomeContext biomeContext) {
        super(other.settings, other.terrainFactory, TerraContext::createCache);
        this.worldSeed = other.worldSeed;
        this.terraSettings = other.terraSettings;
        this.biomeContext = biomeContext;
        this.heightmap = worldGenerator.then(WorldGeneratorFactory::getHeightmap);
    }

    public TerraContext(TerraSettings settings, TFBiomeContext biomeContext) {
        super(settings, TFTerrainProvider::new, TerraContext::createCache);
        this.worldSeed = settings.world.seed;
        this.biomeContext = biomeContext;
        this.terraSettings = settings;
        this.heightmap = worldGenerator.then(WorldGeneratorFactory::getHeightmap);
    }

    protected TerraContext(TerraContext other, int seedOffset) {
        super(other, seedOffset);
        worldSeed = other.worldSeed;
        heightmap = other.heightmap;
        terraSettings = other.terraSettings;
        biomeContext = other.biomeContext;
    }

    @Override
    public TerraContext copy() {
        return new TerraContext(this, 0);
    }

    @Override
    public TerraContext split(int offset) {
        return new TerraContext(this, offset);
    }

    public DecoratorContext decorator(IChunk chunk) {
        return new DecoratorContext(chunk, levels, worldGenerator.get().getClimate(), false);
    }

    public SurfaceContext surface(SurfaceChunk buffer, TFBiomeContainer biomes, DimensionSettings settings) {
        return new SurfaceContext(buffer, biomes, levels, worldGenerator.get().getClimate(), settings, worldSeed);
    }

    protected TerraSettings getTerraSettings() {
        return terraSettings;
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
                .size(tileSize, PerfDefaults.TILE_BORDER)
                .batch(batchCount)
                .factory(factory)
                .build()
                .toCache(false);
    }
}
