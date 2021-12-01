package com.terraforged.mod.worldgen;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.codec.WorldGenCodec;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import com.terraforged.mod.worldgen.util.StructureConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Map;

public class GeneratorCodec implements WorldGenCodec<Generator> {
    @Override
    public <T> Generator decode(DynamicOps<T> ops, T input, RegistryAccess access) {
        long seed = getSeed(ops, input);
        var levels = new TerrainLevels();
        var biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);

        var biomeGenerator = new BiomeGenerator(seed, access);
        var noiseGenerator = new NoiseGenerator(seed, levels, access);
        var biomeSource = new Source(seed, noiseGenerator, biomes);
        var vanillaGenerator = getVanillaChunkGenerator(seed, biomeSource, access);
        var structureConfig = getStructureSettings(access);

        return new Generator(seed, levels, vanillaGenerator, biomeSource, biomeGenerator, noiseGenerator, structureConfig);
    }

    @Override
    public <T> T encode(Generator generator, DynamicOps<T> ops) {
        return ops.createMap(Map.of(ops.createString("seed"), ops.createNumeric(generator.seed)));
    }

    protected static <T> long getSeed(DynamicOps<T> ops, T input) {
        return ops.get(input, "seed")
                .flatMap(ops::getNumberValue)
                .map(Number::longValue)
                .result()
                .orElse(0L);
    }

    protected static NoiseBasedChunkGenerator getVanillaChunkGenerator(long seed, BiomeSource biomes, RegistryAccess access) {
        var noiseParameters = access.registryOrThrow(Registry.NOISE_REGISTRY);
        var noiseSettings = access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        return new NoiseBasedChunkGenerator(noiseParameters, biomes, seed, () -> noiseSettings.getOrThrow(NoiseGeneratorSettings.OVERWORLD));
    }

    protected static StructureConfig getStructureSettings(RegistryAccess access) {
        return access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY)
                .getOptional(NoiseGeneratorSettings.OVERWORLD)
                .map(NoiseGeneratorSettings::structureSettings)
                .map(StructureConfig::new)
                .orElseThrow();
    }
}
