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

package com.terraforged.mod.feature.structure;

import com.google.common.base.Charsets;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.util.quadsearch.QuadSearch;
import com.terraforged.mod.util.quadsearch.SearchContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class StructureLocator {

    public static final String ASYNC_KEY = "async_structure_search";
    public static final String TIMEOUT_KEY = "structure_search_timeout";

    public static final boolean DEFAULT_ASYNC = true;
    public static final long DEFAULT_TIMEOUT_MS = 5_000L;

    private static final String UNSAFE_ERROR = "Async search for structure {} failed! It might not be thread-safe! \nError: {}";
    private static final String FALLBACK_MESSAGE = "Falling back to synchronous search for structure {}";

    // Keep track of structures that are not thread-safe
    private static final Set<Structure<?>> unsafeStructures = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static BlockPos find(BlockPos center, int radius, boolean skipExisting, Structure<?> structure, StructureSeparationSettings settings, IWorld world, TFChunkGenerator generator) {
        final long timeout = ConfigManager.GENERAL.getLong(TIMEOUT_KEY, DEFAULT_TIMEOUT_MS);

        if (world instanceof ServerWorld) {
            boolean async = ConfigManager.GENERAL.getBool(ASYNC_KEY, DEFAULT_ASYNC);
            return search(center, radius, structure, settings, async, timeout, new StructureSearch(
                    center,
                    skipExisting,
                    structure, settings,
                    (ServerWorld) world,
                    generator
            ));
        } else {
            // Probably never happens, but in-case it's not a ServerWorld then run in sync mode
            return search(center, radius, structure, settings, false, timeout, new StructureSearch(
                    center,
                    skipExisting,
                    structure, settings,
                    world,
                    generator
            ));
        }
    }

    private static BlockPos search(BlockPos center, int radius, Structure<?> structure, StructureSeparationSettings settings, boolean async, long timeoutMS, StructureSearch search) {
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;
        int spacing = settings.func_236668_a_();
        SearchContext context = new SearchContext(timeoutMS, TimeUnit.MILLISECONDS);

        if (async && !unsafeStructures.contains(structure)) {
            BlockPos result = QuadSearch.asyncSearch(chunkX, chunkZ, radius, spacing, search, context);
            if (result != null) {
                return result;
            }

            // Check if the search errored. It may have just not found a match
            Throwable error = search.error();
            if (error == null) {
                return null;
            }

            // Keep track of unsafe structures so we don't try again in future
            unsafeStructures.add(structure);
            Log.err(UNSAFE_ERROR, structure.getRegistryName(), getError(error));

            // Return if out of time
            if (context.hasTimedOut()) {
                return null;
            }

            // Reset search & context so that we can fall back to sync search using the remaining time
            search = search.reset();
            context = context.newWithTimeout();
            Log.debug(FALLBACK_MESSAGE, structure.getRegistryName());
        }

        return QuadSearch.search(chunkX, chunkZ, radius, spacing, search, context);
    }

    private static String getError(Throwable throwable) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream(); PrintStream stream = new PrintStream(out)) {
            throwable.printStackTrace(stream);
            return out.toString(Charsets.UTF_8.name());
        } catch (IOException e) {
            return throwable.getLocalizedMessage();
        }
    }
}
