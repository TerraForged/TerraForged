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

package com.terraforged.mod.worldgen;

import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.longs.Long2ObjectLinkedOpenHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.LiteralContents;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.lang.reflect.Field;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class Regenerator {
    private static final Supplier<Field[]> CACHES = Suppliers.memoize(() -> getFields(ChunkMap.class, Long2ObjectLinkedOpenHashMap.class).toArray(Field[]::new));

    public static void regenerateChunks(ChunkPos pos, int radius, ServerLevel level, CommandSourceStack source) {
        log(source, "Deleting chunks", ChatFormatting.ITALIC);
        deleteChunks(pos, radius, level);

        log(source, "Regenerating chunks", ChatFormatting.ITALIC);
        regenerateChunks(level);

        log(source, "Regen complete!", ChatFormatting.GREEN);
    }

    private static void log(CommandSourceStack source, String message, ChatFormatting... formatting) {
        source.sendSuccess(MutableComponent.create(new LiteralContents(message)).withStyle(formatting), true);
    }

    private static void deleteChunks(ChunkPos pos, int radius, ServerLevel level) {
        var chunkSource = level.getChunkSource();
        var caches = getCaches(chunkSource.chunkMap);

        // Flush out any ChunkHolders that are queued to be written to disk
        chunkSource.save(true);
        chunkSource.chunkMap.flushWorker();

        for (int dz = -radius; dz <= radius; dz++) {
            for (int dx = -radius; dx <= radius; dx++) {
                int x = pos.x + dx;
                int z = pos.z + dz;
                var chunkPos = new ChunkPos(x, z);
                long chunkIndex = chunkPos.toLong();

                chunkSource.chunkMap.write(chunkPos, null);

                // Remove ChunkHolder from caches
                for (var cache : caches) {
                    cache.remove(chunkIndex);
                }
            }
        }
    }

    private static void regenerateChunks(ServerLevel level) {
        var chunkSource = level.getChunkSource();
        chunkSource.tick(() -> true, false); // false - tickChunks?
    }

    private static Long2ObjectLinkedOpenHashMap<?>[] getCaches(ChunkMap chunkMap) {
        var fields = CACHES.get();
        var caches = new Long2ObjectLinkedOpenHashMap<?>[fields.length];
        for (int i = 0; i < fields.length; i++) {
            caches[i] = get(chunkMap, fields[i], Long2ObjectLinkedOpenHashMap.class, Long2ObjectLinkedOpenHashMap::new);
        }
        return caches;
    }

    private static <T> T get(Object owner, Field field, Class<T> type, Supplier<T> defaultSupplier) {
        try {
            var t = field.get(owner);
            if (type.isInstance(t)) {
                return type.cast(t);
            }
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return defaultSupplier.get();
    }

    private static Stream<Field> getFields(Class<?> type, Class<?> fieldType) {
        return Stream.of(type.getDeclaredFields()).filter(f -> f.getType() == fieldType).peek(f -> f.setAccessible(true));
    }
}
