/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

import com.google.gson.JsonElement;
import com.mojang.serialization.Codec;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.common.world.StructureSpawnManager;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobGenerator implements Generator.Mobs {

    private static final String DISABLE_MOBS = "disable_mob_generation";
    private static final Codec<DimensionSettings> CODEC = DimensionSettings.DIRECT_CODEC;

    private final boolean mobSpawning;
    private final CatSpawner catSpawner = new CatSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();

    public MobGenerator(TFChunkGenerator generator) {
        DimensionSettings settings = generator.getDimensionSettings().get();
        this.mobSpawning = !Codecs.getField(settings, CODEC, JsonElement::getAsBoolean, DISABLE_MOBS).orElse(false);
    }

    @Override
    public final void generateMobs(WorldGenRegion region) {
        if (mobSpawning) {
            int chunkX = region.getCenterX();
            int chunkZ = region.getCenterZ();
            BiomeContainer biomes = region.getChunk(chunkX, chunkZ).getBiomes();
            if (biomes == null) {
                return;
            }
            Biome biome = biomes.getNoiseBiome(0, 0, 0);
            SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
            sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
            WorldEntitySpawner.spawnMobsForChunkGeneration(region, biome, chunkX, chunkZ, sharedseedrandom);
        }
    }

    @Override
    public final void tickSpawners(ServerWorld world, boolean hostile, boolean peaceful) {
        phantomSpawner.tick(world, hostile, peaceful);
        patrolSpawner.tick(world, hostile, peaceful);
        catSpawner.tick(world, hostile, peaceful);
    }

    @Override
    public final List<MobSpawnInfo.Spawners> getSpawns(Biome biome, StructureManager structures, EntityClassification type, BlockPos pos) {
        List<MobSpawnInfo.Spawners> spawns = StructureSpawnManager.getStructureSpawns(structures, type, pos);
        if (spawns != null) {
            return spawns;
        }

        if (structures.getStructureAt(pos, true, Structure.SWAMP_HUT).isValid()) {
            if (type == EntityClassification.MONSTER) {
                return Structure.SWAMP_HUT.getDefaultSpawnList();
            }

            if (type == EntityClassification.CREATURE) {
                return Structure.SWAMP_HUT.getDefaultCreatureSpawnList();
            }
        }

        if (type == EntityClassification.MONSTER) {
            if (structures.getStructureAt(pos, false, Structure.PILLAGER_OUTPOST).isValid()) {
                return Structure.PILLAGER_OUTPOST.getSpecialEnemies();
            }

            if (structures.getStructureAt(pos, false, Structure.OCEAN_MONUMENT).isValid()) {
                return Structure.OCEAN_MONUMENT.getSpecialEnemies();
            }

            if (structures.getStructureAt(pos, true, Structure.NETHER_BRIDGE).isValid()) {
                return Structure.NETHER_BRIDGE.getSpecialEnemies();
            }
        }

        return biome.getMobSettings().getMobs(type);
    }
}
