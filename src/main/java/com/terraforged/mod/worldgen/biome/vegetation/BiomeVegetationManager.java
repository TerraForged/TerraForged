package com.terraforged.mod.worldgen.biome.vegetation;

import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.asset.ViabilityConfig;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.Biome;

import java.util.IdentityHashMap;
import java.util.Map;

public class BiomeVegetationManager {
    private final Map<Biome, BiomeVegetation> vegetation = new IdentityHashMap<>();
    private final Map<Biome, ViabilityConfig> viability = new IdentityHashMap<>();

    public BiomeVegetationManager(RegistryAccess access) {
        var biomes = access.registryOrThrow(Registry.BIOME_REGISTRY);
        var viabilities = access.registryOrThrow(ModRegistry.VIABILITY);
        for (var entry : biomes.entrySet()) {
            var biome = entry.getValue();
            var vegetation = new BiomeVegetation(entry.getKey(), access);
            var viability = getViability(biome, viabilities);

            this.vegetation.put(biome, vegetation);

            if (viability != null) {
                this.viability.put(biome, viability);
            }
        }
    }

    public BiomeVegetation getVegetation(Biome biome) {
        return vegetation.getOrDefault(biome, BiomeVegetation.NONE);
    }

    public ViabilityConfig getViability(Biome biome) {
        return viability.getOrDefault(biome, ViabilityConfig.NONE);
    }

    private static ViabilityConfig getViability(Biome biome, Registry<ViabilityConfig> registry) {
        return registry.stream().filter(vc -> vc.biomes().get().contains(biome)).findFirst().orElse(null);
    }
}
