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

package com.terraforged.api.chunk;

import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkRandom;

public class ChunkContext {

    public final int chunkX;
    public final int chunkZ;
    public final int blockX;
    public final int blockZ;
    public final Chunk chunk;
    public final ChunkRandom random = new ChunkRandom();

    public ChunkContext(Chunk chunk) {
        this.chunk = chunk;
        this.chunkX = chunk.getPos().x;
        this.chunkZ = chunk.getPos().z;
        this.blockX = chunkX << 4;
        this.blockZ = chunkZ << 4;
        this.random.setSeed(chunkX, chunkZ);
    }
}
