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

package com.terraforged.mod.biome.tag;

import com.terraforged.api.biome.BiomeTags;
import com.terraforged.mod.Log;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.minecraft.resource.ResourceManager;
import net.minecraft.tag.TagContainer;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class BiomeTagManager implements IdentifiableResourceReloadListener {

    private static final boolean RETAIN_ORDER = false;
    private static final Identifier ID = new Identifier("terraforged", "biome_tags");

    private final TagContainer<Biome> collection = new TagContainer<>(
            Registry.BIOME::getOrEmpty,
            "tags/biomes",
            BiomeTagManager.RETAIN_ORDER,
            "biomes"
    );

    @Override
    public Identifier getFabricId() {
        return ID;
    }

    @Override
    public CompletableFuture<Void> reload(Synchronizer synchronizer, ResourceManager manager, Profiler prepareProfiler, Profiler applyProfiler,
            Executor prepareExecutor, Executor applyExecutor) {
        Log.debug("Reloading biome tag collection");
        return collection.prepareReload(manager, prepareExecutor)
                .thenCompose(synchronizer::whenPrepared)
                .thenAcceptAsync(collection::applyReload, applyExecutor)
                .thenRun(() -> {
                    BiomeTags.setCollection(collection);
                    Log.debug("Biome tag reload complete");
                });
    }
}
