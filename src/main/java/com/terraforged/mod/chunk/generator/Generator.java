package com.terraforged.mod.chunk.generator;

import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.server.ServerWorld;

import java.util.List;

public interface Generator {

    interface Biomes {

        /**
         * Generates the biomes for this chunk
         */
        void generateBiomes(IChunk chunk);
    }

    interface Terrain {

        /**
         * Generates the basic heightmap and populates with stone/water/bedrock accordinly
         */
        void generateTerrain(IWorld world, IChunk chunk, StructureManager structures);
    }

    interface Features {

        /**
         * Places biome specific features into the center-chunk of the world gen region
         * The region consists of the center chunk (the chunk being generated) and it's 8 neighbouring chunks (citation needed)
         */
        void generateFeatures(WorldGenRegion region, StructureManager manager);
    }

    interface Structures {

        /**
         * Determines where structures will be placed during chunk gen
         */
        void generateStructureStarts(IChunk chunk, DynamicRegistries registries, StructureManager structures, TemplateManager templates);

        /**
         * Determines where individual structure pieces will be placed based on the start positions
         */
        void generateStructureReferences(ISeedReader world, IChunk chunk, StructureManager structures);
    }

    interface Surfaces {

        /**
         * Applies biome specific surface generation during chunk gen
         */
        void generateSurface(WorldGenRegion world, IChunk chunk);
    }

    interface Carvers {

        /**
         * Cuts caves/ravines during chunk gen according to the carving stage
         */
        void carveTerrain(BiomeManager biomes, IChunk chunk, GenerationStage.Carving type);
    }

    interface Mobs {

        /**
         * Spawns mobs during chunk gen
         */
        void generateMobs(WorldGenRegion region);

        /**
         * Ticks the worlds mob spawners post chunk gen
         */
        void tickSpawners(ServerWorld world, boolean hostile, boolean peaceful);

        /**
         * Gets a list of possible spawns at the given position
         */
        List<MobSpawnInfo.Spawners> getSpawns(Biome biome, StructureManager structures, EntityClassification type, BlockPos pos);
    }
}
