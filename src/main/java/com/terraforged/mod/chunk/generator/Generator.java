/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.chunk.generator;

import com.terraforged.mod.Log;
import com.terraforged.mod.featuremanager.util.identity.Identifier;
import com.terraforged.mod.profiler.watchdog.WarnTimer;
import com.terraforged.mod.profiler.watchdog.WatchdogContext;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.StructureSeparationSettings;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
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

    interface Strongholds {

        boolean isStrongholdChunk(ChunkPos pos);

        @Nullable
        BlockPos findNearestStronghold(BlockPos pos);
    }

    interface Structures {

        StructureSeparationSettings getSeparationSettings(Structure<?> structure);

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

    static void checkTime(String type, Object identity, WarnTimer timer, long timestamp, WatchdogContext context) {
        long duration = timer.since(timestamp);
        if (timer.warn(duration)) {
            context.pushTime(type, identity, duration);
            Log.warn("{} was slow to generate! ({}ms): {}", type, duration, identity);
        }
    }

    static void checkTime(String type, Identifier identity, WarnTimer timer, long timestamp, WatchdogContext context) {
        long duration = timer.since(timestamp);
        if (timer.warn(duration)) {
            context.pushTime(type, identity, duration);
            Log.warn("{} was slow to generate! ({}ms): {}", type, duration, identity.getComponents());
        }
    }
}
