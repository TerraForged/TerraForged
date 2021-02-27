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

package com.terraforged.mod.structure;

import com.terraforged.engine.cell.Cell;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.generator.StructureGenerator;
import com.terraforged.mod.util.quadsearch.Search;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.structure.StructureStart;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class StructureSearch implements Search<BlockPos> {

    protected final long seed;

    private final BlockPos pos;
    private final boolean skipExisting;
    private final Structure<?> structure;
    private final StructureSeparationSettings settings;
    private final TFBiomeProvider biomeProvider;
    private final ThreadLocal<ThreadResource> resource;
    private final AtomicReference<Throwable> errors = new AtomicReference<>();

    public StructureSearch(BlockPos center,
                           boolean skipExisting,
                           Structure<?> structure,
                           StructureSeparationSettings settings,
                           ServerWorld world,
                           TFChunkGenerator generator) {
        this(center, skipExisting, structure, settings, generator.getBiomeProvider(), () -> new FastThreadResource(world, generator));
    }

    public StructureSearch(BlockPos center,
                           boolean skipExisting,
                           Structure<?> structure,
                           StructureSeparationSettings settings,
                           IWorld world,
                           TFChunkGenerator generator) {
        this(center, skipExisting, structure, settings, generator.getBiomeProvider(), () -> new ThreadResource(world));
    }

    public StructureSearch(BlockPos center,
                           boolean skipExisting,
                           Structure<?> structure,
                           StructureSeparationSettings settings,
                           TFBiomeProvider biomeProvider,
                           Supplier<ThreadResource> resourceSupplier) {
        this.seed = biomeProvider.getContext().worldSeed;
        this.pos = center;
        this.skipExisting = skipExisting;
        this.structure = structure;
        this.settings = settings;
        this.biomeProvider = biomeProvider;
        this.resource = ThreadLocal.withInitial(resourceSupplier);
    }

    public StructureSearch reset() {
        errors.set(null);
        return this;
    }

    @Override
    public final BlockPos result() {
        return resource.get().drainResult();
    }

    @Override
    public Throwable error() {
        return errors.get();
    }

    @Override
    public final int compare(BlockPos a, BlockPos b) {
        return Double.compare(a.distanceSq(pos), b.distanceSq(pos));
    }

    @Override
    public final boolean test(int chunkX, int chunkZ) {
        final ThreadResource resource = this.resource.get();

        try {
            ChunkPos pos = structure.getChunkPosForStructure(settings, seed, resource.random, chunkX, chunkZ);

            int biomeX = StructureGenerator.chunkToBiomeChunkCenter(pos.x);
            int biomeZ = StructureGenerator.chunkToBiomeChunkCenter(pos.z);
            Biome biome = biomeProvider.getNoiseBiome(resource.cell.reset(), biomeX, biomeZ);

            if (!biome.getGenerationSettings().hasStructure(structure)) {
                return false;
            }

            IChunk chunk = resource.getChunk(pos);
            StructureStart<?> start = chunk.getStructureStarts().get(structure);
            if (start != null && start.isValid()) {
                if (skipExisting && start.isRefCountBelowMax()) {
                    start.incrementRefCount();
                    resource.setResult(start.getPos());
                    return true;
                }

                if (!skipExisting) {
                    resource.setResult(start.getPos());
                    return true;
                }
            }
        } catch (Throwable t) {
            errors.set(t);
            resource.setResult(null);
            return true;
        }

        return false;
    }

    @Override
    public void close() {
        resource.remove();
    }

    private static class ThreadResource {

        private final IWorld world;
        private final Cell cell = new Cell();
        private final SharedSeedRandom random = new SharedSeedRandom();

        private BlockPos result = null;

        private ThreadResource(IWorld world) {
            this.world = world;
        }

        protected IChunk getChunk(ChunkPos pos) {
            return world.getChunk(pos.x, pos.z, ChunkStatus.STRUCTURE_STARTS);
        }

        protected void setResult(BlockPos pos) {
            this.result = pos;
        }

        protected BlockPos drainResult() {
            BlockPos value = result;
            result = null;
            return value;
        }
    }

    private static class FastThreadResource extends ThreadResource {

        private final ServerWorld world;
        private final TFChunkGenerator generator;
        private final StructureSearchChunk searchChunk = new StructureSearchChunk();

        private FastThreadResource(ServerWorld world, TFChunkGenerator generator) {
            super(world);
            this.world = world;
            this.generator = generator;
        }

        @Override
        protected IChunk getChunk(ChunkPos pos) {
            DynamicRegistries registries = world.func_241828_r();
            StructureManager manager = world.func_241112_a_();
            TemplateManager templates = world.getStructureTemplateManager();
            StructureSearchChunk chunk = searchChunk.init(pos);
            generator.getStructureGenerator().generateStructureStarts(chunk, registries, manager, templates);
            return chunk;
        }
    }
}
