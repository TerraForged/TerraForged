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

package com.terraforged.chunk;

import com.electronwill.nightconfig.core.CommentedConfig;
import com.terraforged.api.chunk.column.DecoratorContext;
import com.terraforged.api.chunk.surface.ChunkSurfaceBuffer;
import com.terraforged.api.chunk.surface.SurfaceContext;
import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.config.PerfDefaults;
import com.terraforged.core.concurrent.thread.ThreadPools;
import com.terraforged.core.tile.gen.TileCache;
import com.terraforged.core.tile.gen.TileGenerator;
import com.terraforged.material.Materials;
import com.terraforged.world.GeneratorContext;
import com.terraforged.world.WorldGeneratorFactory;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.terrain.Terrains;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationSettings;

public class TerraContext extends GeneratorContext {

    public final IWorld world;
    public final Heightmap heightmap;
    public final Materials materials;
    public final TerraSettings terraSettings;

    public TerraContext(TerraContext other) {
        super(other.terrain, other.settings, other.terrainFactory, TerraContext::createCache);
        this.world = other.world;
        this.materials = other.materials;
        this.terraSettings = other.terraSettings;
        this.heightmap = factory.getHeightmap();
    }

    public TerraContext(IWorld world, Terrains terrain, TerraSettings settings) {
        super(terrain, settings, TerraTerrainProvider::new, TerraContext::createCache);
        this.world = world;
        this.materials = new Materials();
        this.terraSettings = settings;
        this.heightmap = factory.getHeightmap();
    }

    public DecoratorContext decorator(IChunk chunk) {
        return new DecoratorContext(chunk, levels, terrain, factory.getClimate(), false);
    }

    public SurfaceContext surface(ChunkSurfaceBuffer buffer, GenerationSettings settings) {
        return new SurfaceContext(buffer, levels, terrain, factory.getClimate(), settings, world.getSeed());
    }

    public static TileCache createCache(WorldGeneratorFactory factory) {
        CommentedConfig config = PerfDefaults.getAndPrintPerfSettings();
        boolean batching = config.getOrElse("batching",false);
        int tileSize = Math.min(PerfDefaults.MAX_TILE_SIZE, Math.max(2, config.getInt("tile_size")));
        int batchCount = Math.min(PerfDefaults.MAX_BATCH_COUNT, Math.max(1, config.getInt("batch_count")));
        int threadCount = Math.min(PerfDefaults.MAX_THREAD_COUNT, Math.max(1, config.getInt("thread_count")));
        return TileGenerator.builder()
                .pool(ThreadPools.create(threadCount, batching))
                .size(tileSize, 2)
                .batch(batchCount)
                .factory(factory)
                .build().toCache();
    }
}
