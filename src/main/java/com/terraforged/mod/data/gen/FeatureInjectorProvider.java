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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.mod.biome.context.TFBiomeContext;
import com.terraforged.mod.featuremanager.matcher.BiomeFeatureMatcher;
import com.terraforged.mod.featuremanager.matcher.biome.BiomeMatcher;
import com.terraforged.mod.featuremanager.matcher.feature.FeatureMatcher;
import com.terraforged.mod.featuremanager.modifier.FeatureModifiers;
import com.terraforged.mod.featuremanager.modifier.Jsonifiable;
import com.terraforged.mod.featuremanager.modifier.Modifier;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DirectoryCache;
import net.minecraft.data.IDataProvider;
import org.apache.commons.lang3.text.translate.JavaUnicodeEscaper;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FeatureInjectorProvider implements IDataProvider {

    private final Path dir;
    private final TFBiomeContext context = TFBiomeContext.dynamic();
    private final Map<String, Modifier<Jsonifiable>> modifiers = new HashMap<>();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private final BiomeMatcher vanillaAndTF;

    public FeatureInjectorProvider(DataGenerator gen, String namespace) {
        dir = gen.getOutputFolder().resolve(Paths.get("data", namespace, "features"));
        vanillaAndTF = BiomeMatcher.of(getContext(), "minecraft:*", "terraforged:*");
    }

    public TFBiomeContext getContext() {
        return context;
    }

    public void add(String path, FeatureMatcher featureMatcher, Jsonifiable modifier) {
        add(path, BiomeMatcher.ANY, featureMatcher, modifier);
    }

    public void addTFVanilla(String path, FeatureMatcher featureMatcher, Jsonifiable modifier) {
        add(path, vanillaAndTF, featureMatcher, modifier);
    }

    public void add(String path, BiomeMatcher biomeMatcher, FeatureMatcher featureMatcher, Jsonifiable modifier) {
        add(path, new BiomeFeatureMatcher(biomeMatcher, featureMatcher), modifier);
    }

    public void add(String path, BiomeFeatureMatcher matcher, Jsonifiable modifier) {
        add(path, new Modifier<>(matcher, modifier));
    }

    public void add(String path, Modifier<Jsonifiable> modifier) {
        modifiers.put(path, modifier);
    }

    public void add(FeatureModifiers modifiers) {}

    @Override
    public String getName() {
        return "Feature_Injectors";
    }

    @Override
    public void run(DirectoryCache cache) throws IOException {
        for (Map.Entry<String, Modifier<Jsonifiable>> entry : modifiers.entrySet()) {
            Path dest = dir.resolve(entry.getKey() + ".json");
            Modifier<Jsonifiable> modifier = entry.getValue();
            JsonObject root = new JsonObject();
            modifier.getMatcher().append(root, context);
            modifier.getModifier().append(root, context);
            write(root, dest, cache);
        }
    }

    @SuppressWarnings("UnstableApiUsage")
    private void write(JsonElement json, Path dest, DirectoryCache cache) throws IOException {
        String data = JavaUnicodeEscaper.outsideOf(0, 0x7f).translate(gson.toJson(json));
        String hash = IDataProvider.SHA1.hashUnencodedChars(data).toString();
        if (!Objects.equals(cache.getHash(dest), hash) || !Files.exists(dest)) {
            Files.createDirectories(dest.getParent());
            try (BufferedWriter bufferedwriter = Files.newBufferedWriter(dest)) {
                bufferedwriter.write(data);
            }
        }
        cache.putNew(dest, hash);
    }
}
