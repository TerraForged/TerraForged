package com.terraforged.mod.chunk.component;

import com.terraforged.mod.chunk.TerraChunkGenerator;
import com.terraforged.mod.chunk.fix.SpawnFix;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;

import java.util.List;

public class MobGenerator {

    private final CatSpawner catSpawner = new CatSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final TerraChunkGenerator generator;

    public MobGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    public final void generateMobs(WorldGenRegion region) {
        // vanilla does NOT check the mobSpawning gamerule before calling this
        if (SpawnFix.canSpawnMobs()) {
            int chunkX = region.getMainChunkX();
            int chunkZ = region.getMainChunkZ();
            Biome biome = region.getChunk(chunkX, chunkZ).getBiomes().getNoiseBiome(0, 0, 0);
            SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
            sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
            WorldEntitySpawner.performWorldGenSpawning(region, biome, chunkX, chunkZ, sharedseedrandom);
        }
    }

    public final void spawnMobs(ServerWorld world, boolean hostile, boolean peaceful) {
        phantomSpawner.tick(world, hostile, peaceful);
        patrolSpawner.tick(world, hostile, peaceful);
        catSpawner.tick(world, hostile, peaceful);
    }

    public final List<Biome.SpawnListEntry> getPossibleCreatures(IWorld world, EntityClassification type, BlockPos pos) {
        if (Feature.SWAMP_HUT.func_202383_b(world, pos)) {
            if (type == EntityClassification.MONSTER) {
                return Feature.SWAMP_HUT.getSpawnList();
            }

            if (type == EntityClassification.CREATURE) {
                return Feature.SWAMP_HUT.getCreatureSpawnList();
            }
        } else if (type == EntityClassification.MONSTER) {
            if (Feature.PILLAGER_OUTPOST.isPositionInStructure(world, pos)) {
                return Feature.PILLAGER_OUTPOST.getSpawnList();
            }

            if (Feature.OCEAN_MONUMENT.isPositionInStructure(world, pos)) {
                return Feature.OCEAN_MONUMENT.getSpawnList();
            }
        }
        return world.getBiome(pos).getSpawns(type);
    }
}
