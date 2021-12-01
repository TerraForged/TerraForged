package com.terraforged.mod.worldgen.feature;

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
