package com.terraforged.mod.worldgen.biome;

import com.mojang.serialization.Codec;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.climate.ClimatePoints;
import com.terraforged.mod.worldgen.biome.climate.Sampler;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

public class Source extends BiomeSource {
    public static final Codec<Source> CODEC = new SourceCodec();

    protected final Registry<Biome> registry;
    protected final Map<Climate.TargetPoint, ResourceKey<Biome>> mapping;

    public Source(RegistryAccess access) {
        this(access.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    public Source(Registry<Biome> biomes) {
        this(biomes, BiomeUtils.getOverworldBiomes(biomes));
    }

    public Source(Registry<Biome> biomes, List<Biome> overworldBiomes) {
        super(overworldBiomes);
        this.registry = biomes;
        this.mapping = getBiomeMapping(biomes, overworldBiomes);
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Source withSeed(long l) {
        return this;
    }

    @Override
    public Biome getNoiseBiome(int x, int y, int z, Climate.Sampler sampler) {
        var point = sampler.sample(x, y, z);
        var key = mapping.get(point);
        var biome = registry.get(key);
        if (biome == null) {
            return registry.getOrThrow(Biomes.PLAINS);
        }
        return biome;
    }

    public Biome getCarvingBiome() {
        return registry.getOrThrow(Biomes.LUSH_CAVES);
    }

    public Registry<Biome> getRegistry() {
        return registry;
    }

    public Climate.Sampler createSampler(long seed, Generator generator) {
        var points = mapping.entrySet().stream()
                .sorted(Comparator.comparing(e -> e.getValue().location()))
                .map(Map.Entry::getKey)
                .toArray(Climate.TargetPoint[]::new);

        return new Sampler(seed, points);
    }

    protected static Map<Climate.TargetPoint, ResourceKey<Biome>> getBiomeMapping(Registry<Biome> registry, List<Biome> biomes) {
        var points = new ClimatePoints();
        var map = new IdentityHashMap<Climate.TargetPoint, ResourceKey<Biome>>();

        map.put(points.next(), Biomes.PLAINS);
        map.put(points.next(), Biomes.FOREST);
        map.put(points.next(), Biomes.TAIGA);
        map.put(points.next(), Biomes.SNOWY_TAIGA);
        map.put(points.next(), Biomes.OLD_GROWTH_PINE_TAIGA);
        map.put(points.next(), Biomes.OLD_GROWTH_SPRUCE_TAIGA);

        if (true) return map;

        for (var biome : biomes) {
            if (biome.getBiomeCategory() == Biome.BiomeCategory.UNDERGROUND) continue;
            if (biome.getBiomeCategory() == Biome.BiomeCategory.OCEAN) continue;
            if (biome.getBiomeCategory() == Biome.BiomeCategory.MUSHROOM) continue;
            if (biome.getBiomeCategory() == Biome.BiomeCategory.RIVER) continue;

            map.put(points.next(), registry.getResourceKey(biome).orElseThrow());
        }

        return map;
    }
}
