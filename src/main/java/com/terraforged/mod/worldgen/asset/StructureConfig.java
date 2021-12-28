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

package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.mod.codec.LazyCodec;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@SuppressWarnings("ClassCanBeRecord")
public class StructureConfig {
    private static final Codec<Map<ResourceLocation, List<ResourceLocation>>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, ResourceLocation.CODEC.listOf());

    public static final Codec<StructureConfig> CODEC = LazyCodec.record(instance -> instance.group(
            Codec.INT.fieldOf("salt").forGetter(c -> c.salt),
            Codec.INT.fieldOf("separation").forGetter(c -> c.separation),
            Codec.INT.fieldOf("spacing").forGetter(c -> c.spacing),
            StructureConfig.MAP_CODEC.fieldOf("feature_biomes").forGetter(c -> c.featureBiomes)
    ).apply(instance, StructureConfig::new));

    private final int salt;
    private final int separation;
    private final int spacing;
    private final Map<ResourceLocation, List<ResourceLocation>> featureBiomes;

    public StructureConfig(int salt, int separation, int spacing, Map<ResourceLocation, List<ResourceLocation>> featureBiomes) {
        this.salt = salt;
        this.separation = separation;
        this.spacing = spacing;
        this.featureBiomes = featureBiomes;
    }

    public StructureFeatureConfiguration createConfig() {
        return new StructureFeatureConfiguration(spacing, separation, salt);
    }

    public void inject(RegistryAccess access, BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> consumer) {
        var features = access.ownedRegistryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        for (var entry : featureBiomes.entrySet()) {
            var feature = parseFeature(entry.getKey(), features);
            if (feature == null) continue;

            for (var biome : entry.getValue()) {
                var biomeKey = parseBiomeKey(biome);
                consumer.accept(feature, biomeKey);
            }
        }
    }

    private static ConfiguredStructureFeature<?, ?> parseFeature(ResourceLocation name, Registry<ConfiguredStructureFeature<?, ?>> registry) {
        return registry.getOptional(name).orElseGet(() -> BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.get(name));
    }

    private static ResourceKey<Biome> parseBiomeKey(ResourceLocation name) {
        return ResourceKey.create(Registry.BIOME_REGISTRY, name);
    }

    private static ResourceLocation encodeFeature(ConfiguredStructureFeature<?, ?> feature, Registry<ConfiguredStructureFeature<?, ?>> registry) {
        var name = registry.getKey(feature);
        if (name != null) return name;

        return BuiltinRegistries.CONFIGURED_STRUCTURE_FEATURE.getKey(feature);
    }

    public static StructureConfig create(StructureFeature<?> structure, StructureSettings settings, RegistryAccess access) {
        var features = access.ownedRegistryOrThrow(Registry.CONFIGURED_STRUCTURE_FEATURE_REGISTRY);
        var structureConfig = settings.getConfig(structure);
        if (structureConfig == null) return null;

        var biomeMappings = new HashMap<ResourceLocation, List<ResourceLocation>>();
        for (var entry : settings.structures(structure).asMap().entrySet()) {
            var feature = encodeFeature(entry.getKey(), features);
            var biomes = biomeMappings.computeIfAbsent(feature, f -> new ArrayList<>());
            for (var biome : entry.getValue()) {
                biomes.add(biome.location());
            }
        }
        return new StructureConfig(structureConfig.salt(), structureConfig.separation(), structureConfig.spacing(), biomeMappings);
    }
}
