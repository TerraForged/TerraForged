package com.terraforged.mod.feature.context;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.cache.SafeCloseable;
import com.terraforged.core.concurrent.pool.ObjectPool;
import com.terraforged.core.tile.chunk.ChunkReader;
import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionDelegate;
import com.terraforged.world.heightmap.Levels;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;

import java.util.Random;

public class ChanceContext implements SafeCloseable {

    private static final ObjectPool<ChanceContext> pool = new ObjectPool<>(10, ChanceContext::new);

    public IChunk chunk;
    public Levels levels;
    public ChunkReader reader;
    public Cell cell = Cell.empty();

    private int length;
    private float total = 0F;
    private float[] buffer;

    @Override
    public void close() {
        if (reader != null) {
            reader.close();
            reader = null;
        }
    }

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
        if (total == 0) {
            return -1;
        }
        float value = 0F;
        float chance = total * random.nextFloat();
        for (int i = 0; i < length; i++) {
            value += buffer[i];
            if (value >= chance) {
                return i;
            }
        }
        return -1;
    }

    public static Resource<ChanceContext> pooled(IWorld world, ChunkGenerator generator) {
        if (generator instanceof TerraChunkGenerator && world instanceof RegionDelegate) {
            TerraChunkGenerator terraGenerator = (TerraChunkGenerator) generator;
            Levels levels = terraGenerator.getContext().levels;
            WorldGenRegion region = ((RegionDelegate) world).getDelegate();
            IChunk chunk = region.getChunk(region.getMainChunkX(), region.getMainChunkZ());
            Resource<ChanceContext> item = pool.get();
            item.get().chunk = chunk;
            item.get().levels = levels;
            item.get().reader = terraGenerator.getChunkReader(chunk.getPos().x, chunk.getPos().z);
            return item;
        }
        return null;
    }
}
