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

import com.terraforged.mod.biome.provider.TFBiomeProvider;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.util.quadsearch.QuadSearch;
import com.terraforged.mod.util.quadsearch.Search;
import com.terraforged.mod.util.quadsearch.SearchContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.TimeUnit;

public class StructureLocator {

    public static final String ASYNC_KEY = "async_structure_search";
    public static final String TIMEOUT_KEY = "structure_search_timeout";

    public static final boolean DEFAULT_ASYNC = true;
    public static final long DEFAULT_TIMEOUT_MS = 5_000L;

    public static BlockPos find(BlockPos center, int radius, boolean skipExisting, Structure<?> structure, StructureSeparationSettings settings, IWorld world, TFBiomeProvider biomeProvider) {
        long timeout = ConfigManager.GENERAL.getLong(TIMEOUT_KEY, DEFAULT_TIMEOUT_MS);

        if (world instanceof ServerWorld) {
            boolean async = ConfigManager.GENERAL.getBool(ASYNC_KEY, DEFAULT_ASYNC);
            return search(center, radius, settings, async, timeout, new StructureSearch(
                    center,
                    skipExisting,
                    structure, settings,
                    (ServerWorld) world,
                    biomeProvider
            ));
        } else {
            // Probably never happens, but in-case it's not a ServerWorld then run in sync mode
            return search(center, radius, settings, false, timeout, new StructureSearch(
                    center,
                    skipExisting,
                    structure, settings,
                    world,
                    biomeProvider
            ));
        }
    }

    private static BlockPos search(BlockPos center, int radius, StructureSeparationSettings settings, boolean async, long timeoutMS, Search<BlockPos> search) {
        int chunkX = center.getX() >> 4;
        int chunkZ = center.getZ() >> 4;
        int spacing = settings.func_236668_a_();
        SearchContext context = new SearchContext(timeoutMS, TimeUnit.MILLISECONDS);
        if (async) {
            return QuadSearch.asyncSearch(chunkX, chunkZ, radius, spacing, search, context);
        }
        return QuadSearch.search(chunkX, chunkZ, radius, spacing, search, context);
    }
}
