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
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.IResourceManager;
import net.minecraft.tags.TagCollection;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class BiomeTagManager implements IFutureReloadListener {

    private static final boolean RETAIN_ORDER = false;

    private final TagCollection<Biome> collection = new TagCollection<>(
            Registry.BIOME::getValue,
            "tags/biomes",
            BiomeTagManager.RETAIN_ORDER,
            "biomes"
    );

    @Override
    public CompletableFuture<Void> reload(IFutureReloadListener.IStage stage, IResourceManager manager, IProfiler preparationsProfiler, IProfiler reloadProfiler, Executor backgroundExecutor, Executor gameExecutor) {
        Log.debug("Reloading biome tag collection");
        return collection.reload(manager, gameExecutor)
                .thenCompose(stage::markCompleteAwaitingOthers)
                .thenAccept(collection::registerAll)
                .thenRun(() -> {
                    BiomeTags.setCollection(collection);
                    Log.debug("Biome tag reload complete");
                });
    }

    @SubscribeEvent
    public static void init(FMLServerAboutToStartEvent event) {
        Log.debug("Registering tag reload listener");
        event.getServer().getResourceManager().addReloadListener(new BiomeTagManager());
    }
}
