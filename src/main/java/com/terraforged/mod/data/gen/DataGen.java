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
import com.terraforged.mod.util.Init;
import com.terraforged.mod.util.json.JsonFormatter;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryWriteOps;
import net.minecraft.resources.ResourceKey;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class DataGen extends Init {
    public static final DataGen INSTANCE = new DataGen();

    @Override
    protected void doInit() {
        if (!Environment.DATA_GEN) return;

        export(Paths.get("datagen"));
    }

    public static void export(Path dir) {
        TerraForged.LOG.info("Generating json data");
        var registries = RegistryAccess.builtin();
        var writeOps = RegistryWriteOps.create(JsonOps.INSTANCE, registries);
        for (var holder : ModRegistries.getHolders()) {
            export(dir, holder, registries, writeOps);
        }
    }

    private static <T> void export(Path dir, ModRegistries.Holder<T> holder, RegistryAccess access, DynamicOps<JsonElement> ops) {
        export(dir, holder.key(), holder.direct(), access, ops);
    }

    private static <T> void export(Path dir, ResourceKey<? extends Registry<T>> key, Codec<T> codec, RegistryAccess access, DynamicOps<JsonElement> ops) {
        var registry = access.ownedRegistryOrThrow(key);
        export(dir, registry, codec, ops);
    }

    private static <T> void export(Path dir, Registry<T> registry, Codec<T> codec, DynamicOps<JsonElement> ops) {
        TerraForged.LOG.info("Exporting registry: {}", registry.key());
        for (var entry : registry.entrySet()) {
            var json = codec.encodeStart(ops, entry.getValue()).result().orElseThrow();
            export(dir, registry.key(), entry.getKey(), json);
        }
    }

    private static void export(Path dir, ResourceKey<?> registry, ResourceKey<?> key, JsonElement json) {
        var file = dir.resolve("data")
                .resolve(registry.location().getNamespace())
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

        try (var out = Files.newBufferedWriter(file)) {
            JsonFormatter.apply(json, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
