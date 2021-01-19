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

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.cache.CacheManager;
import com.terraforged.engine.concurrent.task.LazySupplier;
import com.terraforged.engine.tile.Size;
import com.terraforged.engine.tile.Tile;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.tile.gen.TileCache;
import com.terraforged.mod.Log;
import com.terraforged.mod.api.biome.surface.SurfaceManager;
import com.terraforged.mod.api.chunk.column.BlockColumn;
import com.terraforged.mod.api.chunk.column.ColumnDecorator;
import com.terraforged.mod.api.material.layer.LayerManager;
import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.biome.utils.StructureLocator;
import com.terraforged.mod.chunk.column.ColumnResource;
import com.terraforged.mod.chunk.generator.*;
import com.terraforged.mod.feature.BlockDataManager;
import com.terraforged.mod.featuremanager.FeatureManager;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.featuremanager.util.codec.DecoderFunc;
import com.terraforged.mod.featuremanager.util.codec.EncoderFunc;
import com.terraforged.mod.material.Materials;
import com.terraforged.mod.material.geology.GeoManager;
import com.terraforged.mod.profiler.Profiler;
import com.terraforged.mod.profiler.Section;
import com.terraforged.mod.profiler.crash.CrashHandler;
import com.terraforged.mod.profiler.crash.WorldGenException;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Supplier;

public class TFChunkGenerator extends ChunkGenerator {

    public static final Codec<TFChunkGenerator> CODEC = TFChunkGenerator.codec(TFChunkGenerator::new);

    private final long seed;
    private final TerraContext context;
    private final DimensionSettings settings;
    private final TFBiomeProvider biomeProvider;

    private final Generator.Mobs mobGenerator;
    private final Generator.Biomes biomeGenerator;
    private final Generator.Carvers terrainCarver;
    private final Generator.Terrain terrainGenerator;
    private final Generator.Surfaces surfaceGenerator;
    private final Generator.Features featureGenerator;
    private final Generator.Structures structureGenerator;
    private final Supplier<GeneratorResources> resources;

    public TFChunkGenerator(TFBiomeProvider biomeProvider, DimensionSettings settings) {
        super(biomeProvider, biomeProvider.getSettings().structures.write(settings.getStructures()));
        CacheManager.get().clear();
        TerraContext context = biomeProvider.getContext();
        this.seed = context.terraSettings.world.seed;
        this.context = context;
        this.settings = settings;
        this.biomeProvider = biomeProvider;
        this.mobGenerator = new MobGenerator(this);
        this.biomeGenerator = new BiomeGenerator(this);
        this.terrainCarver = new TerrainCarver(this);
        this.terrainGenerator = new BaseGenerator(this);
        this.surfaceGenerator = new SurfaceGenerator(this);
        this.featureGenerator = new FeatureGenerator(this);
        this.structureGenerator = new StructureGenerator(this);
        this.resources = LazySupplier.factory(context.split(GeneratorResources.SEED_OFFSET), GeneratorResources.factory(this));
        Profiler.reset();
        Log.info("Created TerraForged chunk-generator with settings {}", DataUtils.toJson(context.terraSettings));
    }

    public long getSeed() {
        return seed;
    }

    public DimensionSettings getSettings() {
        return settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return CODEC;
    }

    @Override
    public ChunkGenerator func_230349_a_(long seed) {
        Log.debug("Creating seeded generator: {}", seed);
        TFBiomeProvider biomes = getBiomeProvider().getBiomeProvider(seed);
        return new TFChunkGenerator(biomes, getSettings());
    }

    @Override // getBlockColumn
    public IBlockReader func_230348_a_(int x, int z) {
        float value;
        try (ChunkReader chunkReader = getChunkReader(x >> 4, z >> 4)) {
            value = chunkReader.getCell(x, z).value;
        }

        int height = getContext().levels.scale(value) + 1;
        int surface = Math.max(height, getSeaLevel() + 1);
        BlockColumn column = ColumnResource.get().column.withCapacity(surface);
        BlockState solid = settings.getDefaultBlock();
        for (int y = 0; y < height; y++) {
            column.set(y, solid);
        }

        BlockState fluid = settings.getDefaultFluid();
        for (int y = height; y < surface; y++) {
            column.set(y, fluid);
        }

        return column;
    }

    @Nullable // findStructure
    public BlockPos func_235956_a_(ServerWorld world, Structure<?> structure, BlockPos pos, int radius, boolean flag) {
        if (!this.biomeProvider.hasStructure(structure)) {
            return null;
        }
        if (structure == Structure.STRONGHOLD) {
            return super.func_235956_a_(world, structure, pos, radius, flag);
        }
        StructureSeparationSettings settings = this.settings.getStructures().func_236197_a_(structure);
        if (settings == null) {
            return null;
        }
        return StructureLocator.findStructure(this, world, world.func_241112_a_(), structure, pos, radius, flag, settings);
    }

    @Override
    public final void func_242707_a(DynamicRegistries registries, StructureManager structures, IChunk chunk, TemplateManager templates, long seed) {
        try (Section section = Profiler.STRUCTURE_STARTS.punchIn()) {
            structureGenerator.generateStructureStarts(chunk, registries, structures, templates);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.STRUCTURE_STARTS, t));
        }
    }

    @Override
    public final void func_235953_a_(ISeedReader world, StructureManager structures, IChunk chunk) {
        try (Section section = Profiler.STRUCTURE_REFS.punchIn()) {
            structureGenerator.generateStructureReferences(world, chunk, structures);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.STRUCTURE_REFS, t));
        }
    }

    @Override
    public final void func_242706_a(Registry<Biome> registry, IChunk chunk) {
        try (Section section = Profiler.BIOMES.punchIn()) {
            biomeGenerator.generateBiomes(chunk);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.BIOMES, t));
        }
    }

    @Override
    public final void func_230352_b_(IWorld world, StructureManager structures, IChunk chunk) {
        try (Section section = Profiler.TERRAIN.punchIn()) {
            terrainGenerator.generateTerrain(world, chunk, structures);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.TERRAIN, t));
        }
    }

    @Override
    public final void generateSurface(WorldGenRegion region, IChunk chunk) {
        try (Section section = Profiler.SURFACE.punchIn()) {
            surfaceGenerator.generateSurface(region, chunk);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.SURFACE, t));
        }
    }

    @Override
    public final void func_230350_a_(long seed, BiomeManager biomes, IChunk chunk, GenerationStage.Carving carver) {
        try (Section section = Profiler.CARVING.punchIn()) {
            terrainCarver.carveTerrain(biomes, chunk, carver);
        } catch (Throwable t) {
            CrashHandler.handle(chunk, this, new WorldGenException(Profiler.CARVING, t));
        }
    }

    @Override
    public final void func_230351_a_(WorldGenRegion region, StructureManager structures) {
        try (Section section = Profiler.DECORATION.punchIn()) {
            featureGenerator.generateFeatures(region, structures);
        } catch (Throwable t) {
            CrashHandler.handle(region, this, new WorldGenException(Profiler.DECORATION, t));
        }
    }

    @Override
    public final void func_230354_a_(WorldGenRegion region) {
        try (Section section = Profiler.MOB_SPAWNS.punchIn()) {
            mobGenerator.generateMobs(region);
        } catch (Throwable t) {
            CrashHandler.handle(region, this, new WorldGenException(Profiler.MOB_SPAWNS, t));
        }
    }

    @Override
    public final List<MobSpawnInfo.Spawners> func_230353_a_(Biome biome, StructureManager structures, EntityClassification type, BlockPos pos) {
        return mobGenerator.getSpawns(biome, structures, type, pos);
    }

    @Override
    public final int getHeight(int x, int z, Heightmap.Type type) {
        int chunkX = Size.blockToChunk(x);
        int chunkZ = Size.blockToChunk(z);
        try (ChunkReader chunk = getChunkReader(chunkX, chunkZ)) {
            Cell cell = chunk.getCell(x, z);
            int level = context.levels.scale(cell.value) + 1;
            if (type == Heightmap.Type.OCEAN_FLOOR || type == Heightmap.Type.OCEAN_FLOOR_WG) {
                return level;
            }
            return Math.max(getSeaLevel(), level);
        }
    }

    @Override
    public TFBiomeProvider getBiomeProvider() {
        return biomeProvider;
    }

    @Override
    public final int getMaxBuildHeight() {
        // getMaxHeight
        return getContext().levels.worldHeight;
    }

    @Override
    public final int getSeaLevel() {
        return getContext().levels.waterLevel;
    }

    @Override
    public final int getGroundHeight() {
        return getContext().levels.groundLevel;
    }

    public final TerraContext getContext() {
        return context;
    }

    public final Materials getMaterials() {
        return context.materials.get();
    }

    public final FeatureManager getFeatureManager() {
        return resources.get().featureManager;
    }

    public final GeoManager getGeologyManager() {
        return resources.get().geologyManager;
    }

    public final LayerManager getLayerManager() {
        return getMaterials().getLayerManager();
    }

    public final SurfaceManager getSurfaceManager() {
        return resources.get().surfaceManager;
    }

    public final BlockDataManager getBlockDataManager() {
        return resources.get().blockDataManager;
    }

    public final List<ColumnDecorator> getSurfaceDecorators() {
        return resources.get().surfaceDecorators;
    }

    public final List<ColumnDecorator> getPostProcessors() {
        return resources.get().postProcessors;
    }

    public final void queueChunk(ChunkPos pos) {
        queueChunk(pos.x, pos.z);
    }

    public final void queueChunk(int chunkX, int chunkZ) {
        TileCache tileCache = resources.get().tileCache;
        int rx = tileCache.chunkToRegion(chunkX);
        int rz = tileCache.chunkToRegion(chunkZ);
        tileCache.queueRegion(rx, rz);
    }

    public final Tile getTile(ChunkPos pos) {
        return getTile(pos.x, pos.z);
    }

    public final Tile getTile(int chunkX, int chunkZ) {
        TileCache tileCache = resources.get().tileCache;
        int rx = tileCache.chunkToRegion(chunkX);
        int rz = tileCache.chunkToRegion(chunkZ);
        return tileCache.getTile(rx, rz);
    }

    public final ChunkReader getChunkReader(ChunkPos pos) {
        return getChunkReader(pos.x, pos.z);
    }

    public ChunkReader getChunkReader(int chunkX, int chunkZ) {
        return resources.get().tileCache.getChunk(chunkX, chunkZ);
    }

    public static ChunkReader getChunk(IWorld world, ChunkGenerator generator) {
        if (generator instanceof TFChunkGenerator) {
            TFChunkGenerator terra = (TFChunkGenerator) generator;
            if (world instanceof IChunk) {
                IChunk chunk = (IChunk) world;
                return terra.getChunkReader(chunk.getPos().x, chunk.getPos().z);
            }

            if (world instanceof WorldGenRegion) {
                WorldGenRegion region = (WorldGenRegion) world;
                return terra.getChunkReader(region.getMainChunkX(), region.getMainChunkZ());
            }
        }
        throw new IllegalStateException("NONONO");
    }

    private static <TF extends TFChunkGenerator, T> Dynamic<T> encodeGenerator(TF generator, DynamicOps<T> ops) {
        T biomeProvider = Codecs.encodeAndGet(TFBiomeProvider.CODEC, generator.getBiomeProvider(), ops);
        T dimensionSettings = Codecs.encodeAndGet(DimensionSettings.field_236098_b_, generator::getSettings, ops);
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("biome_provider"), biomeProvider,
                ops.createString("dimension_settings"), dimensionSettings
        )));
    }

    private static <TF extends TFChunkGenerator, T> TF decodeGenerator(Dynamic<T> dynamic, BiFunction<TFBiomeProvider, DimensionSettings, TF> constructor) {
        TFBiomeProvider biomes = Codecs.decodeAndGet(TFBiomeProvider.CODEC, dynamic.get("biome_provider"));
        DimensionSettings settings = Codecs.decodeAndGet(DimensionSettings.field_236098_b_, dynamic.get("dimension_settings")).get();
        return constructor.apply(biomes, settings);
    }

    protected static <TF extends TFChunkGenerator> Codec<TF> codec(BiFunction<TFBiomeProvider, DimensionSettings, TF> constructor) {
        EncoderFunc<TF> encoder = TFChunkGenerator::encodeGenerator;
        DecoderFunc<TF> decoder = new DecoderFunc<TF>() {
            @Override
            public <T> TF _decode(Dynamic<T> dynamic) {
                return TFChunkGenerator.decodeGenerator(dynamic, constructor);
            }
        };
        return Codecs.create(encoder, decoder);
    }
}
