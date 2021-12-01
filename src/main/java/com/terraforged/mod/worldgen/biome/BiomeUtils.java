package com.terraforged.mod.worldgen.biome;

import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class BiomeUtils {
    public static List<Biome> getOverworldBiomes(RegistryAccess access) {
        return getOverworldBiomes(access.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    public static List<Biome> getOverworldBiomes(Registry<Biome> biomes) {
        var sorter = getBiomeSorter(biomes);
        return MultiNoiseBiomeSource.Preset.OVERWORLD.biomeSource(biomes).possibleBiomes().stream()
                .sorted(sorter)
                .toList();
    }

    public static Comparator<Biome> getBiomeSorter(Registry<Biome> biomes) {
        return (o1, o2) -> {
            var k1 = biomes.getKey(o1);
            var k2 = biomes.getKey(o2);
            Objects.requireNonNull(k1);
            Objects.requireNonNull(k2);
            return k1.compareTo(k2);
        };
    }
}
