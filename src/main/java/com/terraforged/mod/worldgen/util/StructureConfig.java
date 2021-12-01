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

package com.terraforged.mod.worldgen.util;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMultimap;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.StrongholdConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.StructureFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public class StructureConfig extends StructureSettings {
    final StructureSettings settings;

    public StructureConfig(StructureSettings settings) {
        super(Optional.empty(), Collections.emptyMap());
        this.settings = new StructureSettings(
                Optional.ofNullable(settings.stronghold()),
                ImmutableMap.copyOf(settings.structureConfig())
        );
    }

    public StructureSettings copy() {
        return new StructureSettings(Optional.ofNullable(settings.stronghold()), settings.structureConfig());
    }

    @Override
    @Nullable
    public StrongholdConfiguration stronghold() {
        return settings.stronghold();
    }

    @Override
    @Nullable
    public StructureFeatureConfiguration getConfig(StructureFeature<?> structureFeature) {
        return settings.getConfig(structureFeature);
    }

    @Override
    @VisibleForTesting
    public Map<StructureFeature<?>, StructureFeatureConfiguration> structureConfig() {
        return settings.structureConfig();
    }

    @Override
    public ImmutableMultimap<ConfiguredStructureFeature<?, ?>, ResourceKey<Biome>> structures(StructureFeature<?> structureFeature) {
        return settings.structures(structureFeature);
    }

    public static StructureSettings access(StructureSettings settings) {
        if (settings.getClass() != StructureConfig.class) throw new Error(new IllegalAccessException());

        return settings;
    }
}
