package com.terraforged.mod.worldgen.feature;

import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.util.DelegateGenerator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.StructureSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureManager;

public class StructureGenerator {
    final long seed;
    final Generator generator;
    final ChunkGenerator delegate;
    final StructureConfig settings;

    public StructureGenerator(long seed, RegistryAccess access) {
        this(seed, null, getStructureSettings(access));
    }

    private StructureGenerator(long seed, Generator generator, StructureConfig settings) {
        this.seed = seed;
        this.generator = generator;
        this.settings = settings;
        this.delegate = generator == null ? null : new DelegateGenerator(seed, generator, settings) {};
    }

    public StructureGenerator withGenerator(long seed, Generator generator) {
        return new StructureGenerator(seed, generator, settings);
    }

    public StructureSettings getSettings() {
        return settings;
    }

    public void generateStarts(long seed,
                               ChunkAccess chunk,
                               StructureFeatureManager structureFeatures,
                               StructureManager structures,
                               RegistryAccess access) {
        delegate.createStructures(access, structureFeatures, chunk, structures, seed);
    }

    public void generateRefs(ChunkAccess chunk, WorldGenLevel level, StructureFeatureManager structureFeatures) {
        delegate.createReferences(level, structureFeatures, chunk);
    }

    public BlockPos find(ServerLevel server, StructureFeature<?> feature, BlockPos pos, int i, boolean first) {
        return delegate.findNearestMapFeature(server, feature, pos, i, first);
    }

    public boolean hasStronghold(ChunkPos pos) {
        return delegate.hasStronghold(pos);
    }

    protected static StructureConfig getStructureSettings(RegistryAccess access) {
        return access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                .getOptional(NoiseGeneratorSettings.OVERWORLD)
                .map(NoiseGeneratorSettings::structureSettings)
                .map(StructureConfig::new)
                .orElseThrow();
    }
}
