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

package com.terraforged.mod.registry.hooks;

import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.Common;
import com.terraforged.mod.codec.Codecs;
import com.terraforged.mod.registry.ModRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class StructureConfigHook {
    private static final ThreadLocal<Holder> LOCAL_ACCESS = ThreadLocal.withInitial(Holder::new);

    public static void injectStructureConfigs(RegistryAccess access) {
        try (var holder = LOCAL_ACCESS.get().assign(access)) {
            var configRegistry = access.ownedRegistryOrThrow(ModRegistry.STRUCTURE_CONFIG.get());
            var noiseRegistry = access.ownedRegistryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
            var overworld = noiseRegistry.getOrThrow(NoiseGeneratorSettings.OVERWORLD);
            var configs = new HashMap<>(overworld.structureSettings().structureConfig());

            for (var entry : configRegistry.entrySet()) {
                var structure = Registry.STRUCTURE_FEATURE.get(entry.getKey().location());

                if (structure == null) {
                    continue;
                }

                // Don't overwrite existing configs
                configs.putIfAbsent(structure, entry.getValue().createConfig());
            }

            var structures = createStructureSettings(overworld.structureSettings(), configs);
            var noiseSettings = createNoiseSettings(overworld, structures);
            noiseRegistry.registerOrOverride(OptionalInt.empty(), NoiseGeneratorSettings.OVERWORLD, noiseSettings, Lifecycle.stable());
        }
    }

    public static void injectStructureBiomeConfigs(BiConsumer<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> consumer) {
        if (!Common.INSTANCE.isDone()) return;

        LOCAL_ACCESS.get().access(access -> {
            var configRegistry = access.ownedRegistryOrThrow(ModRegistry.STRUCTURE_CONFIG.get());
            if (configRegistry.isEmpty()) return;

            for (var config : configRegistry) {
                config.inject(access, consumer);
            }
        });
    }

    private static StructureSettings createStructureSettings(StructureSettings settings, Map<StructureFeature<?>, StructureFeatureConfiguration> configs) {
        return new StructureSettings(Optional.ofNullable(settings.stronghold()), configs);
    }

    private static NoiseGeneratorSettings createNoiseSettings(NoiseGeneratorSettings overworld, StructureSettings structureSettings) {
        return Codecs.modify(overworld, NoiseGeneratorSettings.DIRECT_CODEC, json -> {
            var settings = Codecs.encode(structureSettings, StructureSettings.CODEC);
            json.add("structures", settings);
            return json;
        });
    }

    private static class Holder implements AutoCloseable {
        private RegistryAccess access;

        public Holder assign(RegistryAccess access) {
            this.access = access;
            return this;
        }

        public void access(Consumer<RegistryAccess> consumer) {
            if (access == null) return;

            consumer.accept(access);
        }

        @Override
        public void close() {
            this.access = null;
        }
    }
}
