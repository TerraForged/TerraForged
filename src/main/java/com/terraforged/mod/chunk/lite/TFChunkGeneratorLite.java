package com.terraforged.mod.chunk.lite;

import com.mojang.serialization.Codec;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.cache.Cache;
import com.terraforged.core.module.Ridge;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.core.tile.chunk.ChunkWriter;
import com.terraforged.core.util.PosUtil;
import com.terraforged.mod.Log;
import com.terraforged.mod.biome.provider.TerraBiomeProvider;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.noise.util.NoiseUtil;
import com.terraforged.noise.util.Vec2f;
import com.terraforged.world.WorldGeneratorFactory;
import com.terraforged.world.heightmap.Heightmap;
import com.terraforged.world.rivermap.RiverCache;
import com.terraforged.world.rivermap.Rivermap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;

import java.util.concurrent.TimeUnit;
import java.util.function.LongFunction;

public class TFChunkGeneratorLite extends TFChunkGenerator {

    public static final Codec<TFChunkGeneratorLite> LITE_CODEC = TFChunkGenerator.codec(TFChunkGeneratorLite::new);
    private static final int CACHE_SIZE = 1024;

    private final LongFunction<LiteChunk> chunkGenerator = this::generateChunk;
    private final ChunkWriter.Visitor<Heightmap> posGenerator = this::generatePos;
    private final Cache<LiteChunk> cache = new Cache<>(CACHE_SIZE, 60, 60, TimeUnit.SECONDS);

    public TFChunkGeneratorLite(TerraBiomeProvider biomeProvider, DimensionSettings settings) {
        super(simplifySettings(biomeProvider), settings);
    }

    @Override
    protected Codec<? extends ChunkGenerator> func_230347_a_() {
        return LITE_CODEC;
    }

    @Override
    protected TFChunkGenerator create(long seed) {
        Log.debug("Creating seeded lite generator: {}", seed);
        TerraBiomeProvider biomes = getBiomeProvider().getBiomeProvider(seed);
        return new TFChunkGeneratorLite(biomes, getSettings());
    }

    @Override
    public ChunkReader getChunkReader(int chunkX, int chunkZ) {
        return cache.computeIfAbsent(ChunkPos.asLong(chunkX, chunkZ), chunkGenerator);
    }

    private LiteChunk generateChunk(long chunkId) {
        int chunkX = ChunkPos.getX(chunkId);
        int chunkZ = ChunkPos.getZ(chunkId);
        WorldGeneratorFactory factory = getContext().factory.get();
        LiteChunk chunk = new LiteChunk(chunkX, chunkZ, factory.getHeightmap());
        chunk.generate(factory.getHeightmap(), posGenerator);
        return chunk;
    }

    private void generatePos(Cell cell, int dx, int dz, int x, int z, Heightmap heightmap) {
        heightmap.apply(cell, x, z);
        heightmap.applyRivers(cell, x, z, heightmap.getRivers().getRivers(cell));
    }

    private static TerraBiomeProvider simplifySettings(TerraBiomeProvider provider) {
        TerraSettings settings = provider.getContext().terraSettings;
        settings.miscellaneous.strataDecorator = false;
        return provider;
    }

    private static final long MASK = (((long) 1) << 32) - 1;

    private static float distance(int seed, float x, float y) {
        int ix = NoiseUtil.floor(x);
        int iy = NoiseUtil.floor(y);
        long distance = packf(Float.MAX_VALUE, Float.MAX_VALUE);
        distance = min(seed, x, y, ix - 1, iy - 1, distance);
        distance = min(seed, x, y, ix + 1, iy - 1, distance);
        distance = min(seed, x, y, ix, iy - 1, distance);
        distance = min(seed, x, y, ix - 1, iy, distance);
        distance = min(seed, x, y, ix + 1, iy, distance);
        distance = min(seed, x, y, ix, iy, distance);
        distance = min(seed, x, y, ix - 1, iy + 1, distance);
        distance = min(seed, x, y, ix + 1, iy + 1, distance);
        distance = min(seed, x, y, ix, iy + 1, distance);
        return PosUtil.unpackXf(distance) / PosUtil.unpackYf(distance);
    }

    private static long min(int seed, float x, float y, int cellX, int cellY, long current) {
        Vec2f v = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, cellX, cellY) & 255];
        float dx = cellX + 0.5F + v.x - x;
        float dy = cellY + 0.5F + v.y - y;
        float d = dx * dx + dy * dy;
        float distance = unpackXf(current);
        if (d < distance) {
            return packf(d, distance);
        }
        if (d < unpackYf(current)) {
            return packf(distance, d);
        }
        return current;
    }

    public static float unpackXf(long xy) {
        return Float.intBitsToFloat((int) (xy >>> 32 & MASK));
    }

    public static float unpackYf(long xy) {
        return Float.intBitsToFloat((int) (xy & MASK));
    }

    public static long packf(float x, float y) {
        long left = Float.floatToRawIntBits(x);
        long right = Float.floatToRawIntBits(y);
        return right & MASK | (left & MASK) << 32;
    }
}
