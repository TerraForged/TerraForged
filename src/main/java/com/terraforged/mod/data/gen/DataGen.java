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

package com.terraforged.mod.data.gen;

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.util.FileUtil;
import com.terraforged.mod.util.Init;
import com.terraforged.mod.util.json.JsonFormatter;
import com.terraforged.mod.worldgen.GeneratorPreset;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class DataGen extends Init {
    public static final DataGen INSTANCE = new DataGen();

    @Override
    protected void doInit() {
        if (!Environment.DATA_GEN) return;

        export(Paths.get("datagen"));
    }

    public static void export(Path dir) {
        FileUtil.delete(dir);

        TerraForged.LOG.info("Generating json data");
        var registries = RegistryAccess.builtinCopy();
        var writeOps = RegistryOps.create(JsonOps.INSTANCE, registries);

//        genDimensionType(dir, registries, writeOps);
        genGenerator(dir, registries, writeOps);
        genBuiltin(dir, registries, writeOps);
        genBiomes(dir, registries, writeOps);
    }

    /*public static void exportStructureConfigs(Path dir, StructureSettings structures, RegistryAccess access) {
        for (var entry : structures.structureConfig().entrySet()) {
            var structureName = Registry.STRUCTURE_FEATURE.getKey(entry.getKey());
            if (structureName == null || structureName.getNamespace().equals("minecraft")) continue;

            var structureConfig = StructureConfig.create(entry.getKey(), structures, access);
            if (structureConfig == null) continue;

            export(dir, ModRegistry.STRUCTURE_CONFIG.get(), structureName, StructureConfig.CODEC, structureConfig, JsonOps.INSTANCE);
        }
    }*/

    private static void genGenerator(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var generator = GeneratorPreset.getDefault(registries);
        var json = LevelStem.CODEC.encodeStart(writeOps, generator).resultOrPartial(System.err::println).orElseThrow();
        export(dir, Registry.DIMENSION_REGISTRY, Level.OVERWORLD, json);
    }

    private static void genDimensionType(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        export(dir, Registry.DIMENSION_TYPE_REGISTRY, DimensionType.DIRECT_CODEC, registries, writeOps);
    }

    private static void genBuiltin(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        for (var holder : ModRegistries.getHolders()) {
            export(dir, holder, registries, writeOps);
        }
    }

    private static void genBiomes(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var biomes = registries.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
        for (var entry : biomes.entrySet()) {
            if (!entry.getKey().location().getNamespace().equals(TerraForged.MODID)) continue;

            var json = Biome.DIRECT_CODEC.encodeStart(writeOps, entry.getValue()).result().orElseThrow();
            export(dir, biomes.key(), entry.getKey(), json);
        }
    }

    private static <T> void export(Path dir, ModRegistries.HolderEntry<T> holder, RegistryAccess access, DynamicOps<JsonElement> ops) {
        export(dir, holder.key(), holder.direct(), access, ops);
    }

    private static <T> void export(Path dir, ResourceKey<? extends Registry<T>> key, Codec<T> codec, RegistryAccess access, DynamicOps<JsonElement> ops) {
        var registry = access.ownedRegistryOrThrow(key);
        export(dir, registry, codec, ops);
    }

    private static <T> void export(Path dir, Registry<T> registry, Codec<T> codec, DynamicOps<JsonElement> ops) {
        TerraForged.LOG.info("Exporting registry: {}", registry.key());
        for (var entry : registry.entrySet()) {
            try {
                var json = codec.encodeStart(ops, entry.getValue()).result().orElseThrow();
                export(dir, registry.key(), entry.getKey(), json);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }
    }

    private static <T> void export(Path dir, ResourceKey<Registry<T>> registry, ResourceLocation name, Codec<T> codec, T value, DynamicOps<JsonElement> ops) {
        var json = codec.encodeStart(ops, value).result().orElseThrow();

        var file = dir.resolve("data")
                .resolve(name.getNamespace())
                .resolve(registry.location().getPath())
                .resolve(name.getPath() + ".json");

        FileUtil.write(file, json, (writer, data) -> new JsonFormatter(writer).write(data));
    }

    private static void export(Path dir, ResourceKey<?> registry, ResourceKey<?> key, JsonElement json) {
        var file = dir.resolve("data")
                .resolve(key.location().getNamespace())
                .resolve(registry.location().getPath())
                .resolve(key.location().getPath() + ".json");

        var parent = file.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try {
            CompletableFuture.runAsync(() -> {

                try (var out = Files.newBufferedWriter(file)) {
                    JsonFormatter.apply(json, out);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }).get(1, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
