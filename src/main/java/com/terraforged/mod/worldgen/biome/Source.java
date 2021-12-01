package com.terraforged.mod.worldgen.biome;

import com.mojang.serialization.Codec;
import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.world.biome.type.BiomeType;
import com.terraforged.engine.world.climate.ClimateModule;
import com.terraforged.mod.util.map.WeightMap;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.core.Registry;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.Climate;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Source extends BiomeSource {
    public static final Codec<Source> CODEC = new SourceCodec();

    protected final long seed;
    protected final ClimateModule climate;
    protected final Map<BiomeType, WeightMap<Biome>> biomeMap;
    protected final ThreadLocal<Cell> localCell = ThreadLocal.withInitial(Cell::new);

    protected final Registry<Biome> registry;

    public Source(long seed, NoiseGenerator noise, Source other) {
        super(List.copyOf(other.possibleBiomes()));
        this.seed = seed;
        this.registry = other.registry;
        this.biomeMap = other.biomeMap;
        this.climate = createClimate(noise);
    }

    public Source(long seed, NoiseGenerator noise, Registry<Biome> biomes) {
        this(seed, noise, biomes, BiomeUtils.getOverworldBiomes(biomes));
    }

    public Source(long seed, NoiseGenerator noise, Registry<Biome> registry, List<Biome> biomes) {
        super(biomes);
        this.seed = seed;
        this.registry = registry;
        this.climate = createClimate(noise);
        this.biomeMap = getBiomeMapping(registry, biomes);
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
        var cell = localCell.get().reset();
        climate.apply(cell, x << 2, z << 2);

        var biomes = biomeMap.get(cell.biome);
        if (biomes == null) {
            return registry.getOrThrow(Biomes.PLAINS);
        }

        return biomes.getValue(cell.biomeRegionId);
    }

    public Biome getCarvingBiome() {
        return registry.getOrThrow(Biomes.LUSH_CAVES);
    }

    public Registry<Biome> getRegistry() {
        return registry;
    }

    protected static ClimateModule createClimate(NoiseGenerator generator) {
        if (generator == null) return null;
        return new ClimateModule(generator.getContinent(), generator.getContinent().getContext());
    }

    protected static Map<BiomeType, WeightMap<Biome>> getBiomeMapping(Registry<Biome> registry, List<Biome> biomes) {
        var types = getBiomeTypeMap(registry, biomes);
        var mapping = new EnumMap<BiomeType, WeightMap<Biome>>(BiomeType.class);

        for (var type : BiomeType.values()) {
            var list = types.get(type);
            if (list == null) continue;

            var map = getWeightedMap(list);
            mapping.put(type, map);
        }

        return mapping;
    }

    protected static WeightMap<Biome> getWeightedMap(List<Biome> biomes) {
        var biome = biomes.toArray(Biome[]::new);
        var weights = new float[biomes.size()];
        Arrays.fill(weights, 1.0F);
        return new WeightMap<>(biome, weights);
    }

    protected static Map<BiomeType, List<Biome>> getBiomeTypeMap(Registry<Biome> registry, List<Biome> biomes) {
        return biomes.stream()
                .sorted(BiomeUtils.getBiomeSorter(registry))
                .collect(Collectors.groupingBy(Source::getType));
    }

    protected static BiomeType getType(Biome biome) {
        return switch (biome.getBiomeCategory()) {
            case PLAINS -> BiomeType.GRASSLAND;
            case MESA, DESERT -> BiomeType.DESERT;
            case TAIGA -> getTaiga(biome);
            case ICY -> BiomeType.TUNDRA;
            case SAVANNA -> BiomeType.SAVANNA;
            case JUNGLE -> BiomeType.TROPICAL_RAINFOREST;
            case FOREST -> BiomeType.TEMPERATE_FOREST;
            case SWAMP -> BiomeType.TEMPERATE_RAINFOREST;
            default -> BiomeType.ALPINE;
        };
    }

    protected static BiomeType getTaiga(Biome biome) {
        return biome.getBaseTemperature() < 0.2 ? BiomeType.TUNDRA : BiomeType.TAIGA;
    }

    public static class NoopSampler implements Climate.Sampler {
        public static final NoopSampler INSTANCE = new NoopSampler();
        public static final Climate.TargetPoint DEFAULT_POINT = new Climate.TargetPoint(0, 0, 0, 0, 0, 0);

        @Override
        public Climate.TargetPoint sample(int i, int i1, int i2) {
            return DEFAULT_POINT;
        }
    }
}
