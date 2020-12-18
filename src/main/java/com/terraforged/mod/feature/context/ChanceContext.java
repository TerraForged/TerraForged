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

package com.terraforged.mod.feature.context;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.concurrent.Resource;
import com.terraforged.engine.concurrent.cache.SafeCloseable;
import com.terraforged.engine.concurrent.pool.ObjectPool;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.fix.RegionDelegate;
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
        if (generator instanceof TFChunkGenerator && world instanceof RegionDelegate) {
            TFChunkGenerator terraGenerator = (TFChunkGenerator) generator;
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
