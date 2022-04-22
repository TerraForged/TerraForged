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

package com.terraforged.mod.worldgen.util.delegate;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;

public class DelegateRegion extends WorldGenRegion {
    public static final ThreadLocal<Builder> LOCAL_BUILDER = ThreadLocal.withInitial(Builder::new);

    public DelegateRegion(ServerLevel level, List<ChunkAccess> chunks, ChunkStatus status, ChunkStatus stage) {
        super(level, chunks, status, getCutoffRadius(stage));
    }

    public interface Factory<T extends WorldGenRegion> {
        T build(ServerLevel level, List<ChunkAccess> chunks, ChunkStatus centerStatus, ChunkStatus stage);
    }

    public static int getCutoffRadius(ChunkStatus status) {
        int cutoff = 0;
        if (status == ChunkStatus.STRUCTURE_REFERENCES) cutoff = -1;
        else if (status == ChunkStatus.BIOMES) cutoff = -1;
        else if (status == ChunkStatus.FEATURES) cutoff = 1;
        else if (status == ChunkStatus.SPAWN) cutoff = -1;
        return cutoff;
    }

    public static class Builder {
        private ServerLevel level;
        private ChunkStatus status;
        private final List<ChunkAccess> chunks = new ArrayList<>(17 * 17);

        @SuppressWarnings("deprecation")
        public Builder source(WorldGenRegion region) {
            this.chunks.clear();
            this.level = region.getLevel();
            this.status = ChunkStatus.EMPTY;

            var center = region.getCenter();
            for (int dz = -8; dz <= 8; dz++) {
                for (int dx = -8; dx <= 8; dx++) {
                    int cx = center.x + dx;
                    int cz = center.z + dz;

                    if (!region.hasChunk(cx, cz)) continue;

                    var chunk = region.getChunk(cx, cz);
                    chunks.add(chunk);

                    if (dx == 0 && dz == 0) {
                        status = chunk.getStatus();
                    }
                }
            }

            return this;
        }

        public <T extends WorldGenRegion> T build(ChunkStatus stage, Factory<T> factory) {
            return factory.build(level, chunks, status, stage);
        }
    }
}
