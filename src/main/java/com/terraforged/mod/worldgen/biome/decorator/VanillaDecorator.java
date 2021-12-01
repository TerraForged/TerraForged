package com.terraforged.mod.worldgen.biome.decorator;

import com.terraforged.mod.worldgen.Generator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.*;
import java.util.function.Supplier;

public class VanillaDecorator {
    public static void decorate(long seed,
                                int from, int to,
                                BlockPos origin,
                                Biome biome,
                                ChunkAccess chunk,
                                WorldGenLevel level,
                                Generator generator,
                                WorldgenRandom random,
                                StructureFeatureManager structureManager,
                                FeatureDecorator decorator) {

        for (int stage = from; stage <= to; stage++) {
            var structures = decorator.getStageStructures(stage);
            var features = decorator.getStageFeatures(stage, biome);
            int offset = structures.size();

            placeStructures(seed, stage, chunk, level, generator, random, structureManager, structures);

            placeFeatures(seed, offset, stage, origin, level, generator, random, features);
        }
    }

    private static void placeStructures(long seed,
                                        int stage,
                                        ChunkAccess chunk,
                                        WorldGenLevel level,
                                        Generator generator,
                                        WorldgenRandom random,
                                        StructureFeatureManager structureManager,
                                        List<Supplier<StructureFeature<?>>> structures) {

        var chunkPos = chunk.getPos();
        var sectionPos = SectionPos.of(chunkPos, level.getMinSection());

        for (int i = 0; i < structures.size(); i++) {
            random.setFeatureSeed(seed, i, stage);

            var structure = structures.get(i).get();

            structureManager.startsForFeature(sectionPos, structure).forEach(start -> {
                start.placeInChunk(level, structureManager, generator, random, getWritableArea(chunk), chunkPos);
            });
        }
    }

    private static void placeFeatures(long seed,
                                      int offset,
                                      int stage,
                                      BlockPos origin,
                                      WorldGenLevel level,
                                      Generator generator,
                                      WorldgenRandom random,
                                      List<Supplier<PlacedFeature>> features) {

        for (int i = 0; i < features.size(); i++) {
            random.setFeatureSeed(seed, offset + i, stage);

            var feature = features.get(i).get();
            feature.placeWithBiomeCheck(level, generator, random, origin);
        }
    }

    public static Map<GenerationStep.Decoration, List<Supplier<StructureFeature<?>>>> buildStructureMap(RegistryAccess access) {
        final var map = new EnumMap<GenerationStep.Decoration, List<Supplier<StructureFeature<?>>>>(GenerationStep.Decoration.class);
        final var registry = access.registryOrThrow(Registry.STRUCTURE_FEATURE_REGISTRY);

        for (var entry : registry.entrySet()) {
            var key = entry.getKey();
            var value = entry.getValue();
            map.computeIfAbsent(value.step(), s -> new ArrayList<>()).add(() -> registry.get(key));
        }

        for (var stage : FeatureDecorator.STAGES) {
            if (!map.containsKey(stage)) {
                map.put(stage, Collections.emptyList());
            }
        }

        return map;
    }

    private static BoundingBox getWritableArea(ChunkAccess chunkAccess) {
        var chunkPos = chunkAccess.getPos();
        int minX = chunkPos.getMinBlockX();
        int minZ = chunkPos.getMinBlockZ();

        LevelHeightAccessor levelHeightAccessor = chunkAccess.getHeightAccessorForGeneration();
        int minY = levelHeightAccessor.getMinBuildHeight() + 1;
        int maxY = levelHeightAccessor.getMaxBuildHeight() - 1;

        return new BoundingBox(minX, minY, minZ, minX + 15, maxY, minZ + 15);
    }
}
