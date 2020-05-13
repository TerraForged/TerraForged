package com.terraforged.mod.feature.context;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.chunk.ChunkReader;
import com.terraforged.core.concurrent.ObjectPool;
import com.terraforged.world.heightmap.Levels;
import com.terraforged.mod.chunk.util.TerraContainer;
import com.terraforged.mod.chunk.fix.RegionDelegate;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.Random;

public class ChanceContext {

    private static final ObjectPool<ChanceContext> pool = new ObjectPool<>(10, ChanceContext::new);

    public IChunk chunk;
    public Levels levels;
    public ChunkReader reader;
    public Cell cell = Cell.empty();

    private int length;
    private float total = 0F;
    private float[] buffer;

    void setPos(BlockPos pos) {
        cell = reader.getCell(pos.getX(), pos.getZ());
    }

    void init(int size) {
        total = 0F;
        length = 0;
        if (buffer == null || buffer.length < size) {
            buffer = new float[size];
        }
    }

    void record(int index, float chance) {
        buffer[index] = chance;
        total += chance;
        length++;
    }

    int nextIndex(Random random) {
        float value = 0F;
        float chance = random.nextFloat() * total;
        for (int i = 0; i < length; i++) {
            value += buffer[i];
            if (value >= chance) {
                return i;
            }
        }
        return -1;
    }

    public static ObjectPool.Item<ChanceContext> pooled(IWorld world) {
        if (world instanceof RegionDelegate) {
            Levels levels = new Levels(world.getMaxHeight(), world.getSeaLevel());
            WorldGenRegion region = ((RegionDelegate) world).getRegion();
            IChunk chunk = region.getChunk(region.getMainChunkX(), region.getMainChunkZ());
            return pooled(chunk, levels);
        }
        return null;
    }

    public static ObjectPool.Item<ChanceContext> pooled(IChunk chunk, Levels levels) {
        BiomeContainer container = chunk.getBiomes();
        if (container instanceof TerraContainer) {
            ChunkReader reader = ((TerraContainer) container).getChunkReader();
            ObjectPool.Item<ChanceContext> item = pool.get();
            ChanceContext context = item.getValue();
            context.chunk = chunk;
            context.levels = levels;
            context.reader = reader;
            return item;
        }
        return null;
    }
}
