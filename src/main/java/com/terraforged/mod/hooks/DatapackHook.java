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

package com.terraforged.mod.hooks;

import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.ReflectionUtil;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldCreationContext;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.repository.*;
import net.minecraft.world.level.levelgen.presets.WorldPreset;

import java.lang.invoke.MethodHandle;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DatapackHook {
    private static final String PACK_FILE_ID = "file/" + DataPackExporter.PACK_FILE_NAME;

    public static WorldCreationContext createContext(WorldCreationContext context) {
        if (Generator.isTerraForged(context.worldGenSettings().overworld())) {
            return new WorldCreationContext(context.worldGenSettings(), Lifecycle.stable(), context.registryAccess(), context.dataPackResources());
        }
        return context;
    }

    public static RepositorySource[] injectRepositorySource(RepositorySource[] sources) {
        var copy = Arrays.copyOf(sources, sources.length + 1);
        copy[sources.length] = new TerraForgedRepositorySource();
        return copy;
    }

    public static void injectDatapack(PackRepository repository, Path dir) {
        if (!repository.isAvailable(PACK_FILE_ID)) {
            // Copy default datapack to world's temp-dir
            DataPackExporter.createWorldDatapack(dir);

            // Scan temp-dir and insert pack entry into repository
            TerraForgedRepositorySource.inject(repository, dir);

            TerraForged.LOG.info("Injected datapack {}", PACK_FILE_ID);
        }

        var selected = repository.getSelectedIds();
        if (!selected.contains(PACK_FILE_ID)) {
            // Make mutable & add the TF datapack id
            selected = new ArrayList<>(selected);
            selected.add(PACK_FILE_ID);

            // Update the repository with new selection
            repository.setSelected(selected);

            TerraForged.LOG.info("Selected datapack {}", PACK_FILE_ID);
        }
    }

    public static void selectPreset(Object object) {
        if (object instanceof CreateWorldScreen screen) {
            for (var listener : screen.children()) {
                if (listener instanceof CycleButton<?> button && isPreset(button)) {

                    // Only log if the preset changed from a non-TF to the TF preset
                    if (selectPreset(button, id -> id.getNamespace().equals(TerraForged.MODID))) {
                        TerraForged.LOG.info("Selected terraforged world_preset");
                    }

                    return;
                }
            }
        }
    }

    private static boolean selectPreset(CycleButton<?> button, Predicate<ResourceLocation> predicate) {
        var key = getKey(button);
        if (predicate.test(key.location())) {
            return false;
        }

        var next = nextKey(button);
        while (next != key) {

            if (predicate.test(key.location())) {
                return true;
            }

            key = nextKey(button);
        }

        return false;
    }

    private static ResourceKey<?> getKey(CycleButton<?> button) {
        return ((Holder<?>) button.getValue()).unwrapKey().orElseThrow();
    }

    private static ResourceKey<?> nextKey(CycleButton<?> button) {
        button.onPress();
        return getKey(button);
    }

    private static boolean isPreset(CycleButton<?> button) {
        return button.getValue() instanceof Holder<?> holder && holder.unwrap().map(
                key -> key.registry().equals(Registry.WORLD_PRESET_REGISTRY.location()),
                value -> value instanceof WorldPreset
        );
    }

    public static class TerraForgedRepositorySource implements RepositorySource {
        private static final MethodHandle PACK_SOURCES = ReflectionUtil.field(PackRepository.class, Set.class);
        private static final RepositorySource NOOP = (consumer, constructor) -> {};

        protected RepositorySource source = NOOP;

        public void setDir(Path path) {
            source = new FolderRepositorySource(path.toFile(), PackSource.DEFAULT);
        }

        @Override
        public void loadPacks(Consumer<Pack> pack, Pack.PackConstructor constructor) {
            source.loadPacks(pack, constructor);
        }

        public static void inject(PackRepository repository, Path dir) {
            try {
                var set = (Set<?>) PACK_SOURCES.invokeExact(repository);

                for (var object : set) {
                    if (object instanceof TerraForgedRepositorySource source) {
                        source.setDir(dir);
                        return;
                    }
                }
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
    }
}
