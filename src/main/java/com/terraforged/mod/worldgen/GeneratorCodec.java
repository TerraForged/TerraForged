package com.terraforged.mod.worldgen;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.codec.WorldGenCodec;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.feature.StructureGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainGenerator;
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
        var biomeSource = new Source(access);
        var biomeGenerator = new BiomeGenerator(seed, access);
        var terrainGenerator = new TerrainGenerator(seed, access);
        var structureGenerator = new StructureGenerator(seed, access);
        var vanillaGenerator = getVanillaChunkGenerator(seed, biomeSource, access);
        return new Generator(seed, vanillaGenerator, biomeSource, biomeGenerator, terrainGenerator, structureGenerator);
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
}
