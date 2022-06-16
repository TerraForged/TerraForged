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

import com.google.common.hash.Hashing;
import com.google.common.hash.HashingOutputStream;
import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.CommonAPI;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.data.codec.Codecs;
import com.terraforged.mod.data.util.JsonFormatter;
import com.terraforged.mod.lifecycle.Init;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.FileUtil;
import com.terraforged.mod.worldgen.GeneratorPreset;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.CachedOutput;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraft.world.level.levelgen.presets.WorldPresets;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class DataGen extends Init {
    public static final DataGen INSTANCE = new DataGen();

    private final CachedOutput cache;
    private final List<CompletableFuture<?>> tasks = new ObjectArrayList<>();

    public DataGen() {
        cache = null;
    }

    public DataGen(CachedOutput cache) {
        this.cache = cache;
    }

    @Override
    protected void doInit() {
        if (!Environment.DATA_GEN) return;

        doExport(Paths.get("datagen")).join();
    }

    protected CompletableFuture<?> doExport(Path dir) {
        FileUtil.delete(dir);

        var registries = RegistryAccess.builtinCopy();
        var writeOps = RegistryOps.create(JsonOps.INSTANCE, registries);

        genDimensionType(dir, registries, writeOps);
//        genPreset(dir, registries, writeOps);
        genBuiltin(dir, registries, writeOps);
        genBiomes(dir, registries, writeOps);

        return CompletableFuture.allOf(tasks.toArray(CompletableFuture[]::new));
    }

    public static CompletableFuture<?> export(Path dir, CachedOutput cache) {
        return new DataGen(cache).doExport(dir);
    }

    private void genPreset(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var normal = registries.ownedRegistryOrThrow(Registry.WORLD_PRESET_REGISTRY)
                .getOrThrow(WorldPresets.NORMAL);

        var json = Codecs.encode(normal, WorldPreset.DIRECT_CODEC, writeOps).getAsJsonObject();
        var dimension = GeneratorPreset.getDefault(registries);
        var dimensionJson = Codecs.encode(dimension, LevelStem.CODEC, writeOps);

        var dimensions = json.getAsJsonObject("dimensions");
        dimensions.add(LevelStem.OVERWORLD.location().toString(), dimensionJson);

        export(dir, Registry.WORLD_PRESET_REGISTRY, TerraForged.WORLD_PRESET, json);
    }

    private void genDimensionType(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var registry = registries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        var overworld = registry.getOrThrow(BuiltinDimensionTypes.OVERWORLD);

        var json = Codecs.encode(overworld, DimensionType.DIRECT_CODEC, writeOps).getAsJsonObject();
        json.addProperty("height", 1024);
        json.addProperty("logical_height", 1024);
        json.addProperty("effects", TerraForged.DIMENSION_EFFECTS.toString());

        export(dir, Registry.DIMENSION_TYPE_REGISTRY, BuiltinDimensionTypes.OVERWORLD, json);
    }

    private void genBuiltin(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var registryManager = CommonAPI.get().getRegistryManager();
        for (var registry : registryManager.getModRegistries()) {
            export(dir, registry, registries, writeOps);
        }
    }

    private void genBiomes(Path dir, RegistryAccess registries, RegistryOps<JsonElement> writeOps) {
        var biomes = registries.ownedRegistryOrThrow(Registry.BIOME_REGISTRY);
        for (var entry : biomes.entrySet()) {
            if (!entry.getKey().location().getNamespace().equals(TerraForged.MODID)) continue;

            var json = Biome.DIRECT_CODEC.encodeStart(writeOps, entry.getValue()).result().orElseThrow();
            export(dir, biomes.key(), entry.getKey(), json);
        }
    }

    private <T> void export(Path dir, ModRegistry<T> registry, RegistryAccess access, DynamicOps<JsonElement> ops) {
        export(dir, registry.key().get(), registry.codec(), access, ops);
    }

    private <T> void export(Path dir, ResourceKey<? extends Registry<T>> key, Codec<T> codec, RegistryAccess access, DynamicOps<JsonElement> ops) {
        var registry = access.ownedRegistryOrThrow(key);
        export(dir, registry, codec, ops);
    }

    private <T> void export(Path dir, Registry<T> registry, Codec<T> codec, DynamicOps<JsonElement> ops) {
        TerraForged.LOG.info("Exporting registry: {}", registry.key());
        for (var entry : registry.entrySet()) {
            try {
                var json = codec.encodeStart(ops, entry.getValue())
                        .mapError(DataGen::logError)
                        .result()
                        .orElseThrow();

                export(dir, registry.key(), entry.getKey(), json);
            } catch (Throwable t) {
                new EncodingException(entry.getKey(), t).printStackTrace();
            }
        }
    }

    private <T> void export(Path dir, ResourceKey<Registry<T>> registry, ResourceLocation name, Codec<T> codec, T value, DynamicOps<JsonElement> ops) {
        var json = codec.encodeStart(ops, value).result().orElseThrow();

        var file = dir.resolve("data")
                .resolve(name.getNamespace())
                .resolve(registry.location().getPath())
                .resolve(name.getPath() + ".json");

        FileUtil.write(file, json, (writer, data) -> new JsonFormatter(writer).write(data));
    }

    private void export(Path dir, ResourceKey<?> registry, ResourceKey<?> key, JsonElement json) {
        export(dir, registry, key.location(), json);
    }

    private void export(Path dir, ResourceKey<?> registry, ResourceLocation name, JsonElement json) {
        var file = dir.resolve("data")
                .resolve(name.getNamespace())
                .resolve(registry.location().getPath())
                .resolve(name.getPath() + ".json");

        if (cache != null) {
            writeCached(file, json);
        } else {
            tasks.add(CompletableFuture.runAsync(() -> writeDirect(file, json)));
        }
    }

    protected void writeDirect(Path path, JsonElement json) {
        var parent = path.getParent();
        if (!Files.exists(parent)) {
            try {
                Files.createDirectories(parent);
            } catch (IOException e) {
                e.printStackTrace();
                return;
            }
        }

        try (var out = Files.newBufferedWriter(path)) {
            JsonFormatter.format(json, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    protected void writeCached(Path path, JsonElement json) {
        try {
            var byteOut = new ByteArrayOutputStream();
            var hashOut = new HashingOutputStream(Hashing.sha1(), byteOut);

            JsonFormatter.format(json, hashOut);

            cache.writeIfNeeded(path, byteOut.toByteArray(), hashOut.hash());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static String logError(String s) {
        TerraForged.LOG.warn(s);
        return s;
    }
}
