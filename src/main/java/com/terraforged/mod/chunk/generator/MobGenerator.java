package com.terraforged.mod.chunk.generator;

import com.terraforged.mod.chunk.TerraChunkGenerator;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.MobSpawnInfo;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class MobGenerator implements Generator.Mobs {

    // may be accessed cross-thread
    private static volatile boolean mobSpawning = true;

    private final CatSpawner catSpawner = new CatSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final TerraChunkGenerator generator;

    public MobGenerator(TerraChunkGenerator generator) {
        this.generator = generator;
    }

    @Override
    public final void generateMobs(WorldGenRegion region) {
        // vanilla does NOT check the mobSpawning gamerule before calling this
        if (MobGenerator.mobSpawning) {
            int chunkX = region.getMainChunkX();
            int chunkZ = region.getMainChunkZ();
            Biome biome = region.getChunk(chunkX, chunkZ).getBiomes().getNoiseBiome(0, 0, 0);
            SharedSeedRandom sharedseedrandom = new SharedSeedRandom();
            sharedseedrandom.setDecorationSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
            WorldEntitySpawner.performWorldGenSpawning(region, biome, chunkX, chunkZ, sharedseedrandom);
        }
    }

    @Override
    public final void tickSpawners(ServerWorld world, boolean hostile, boolean peaceful) {
        phantomSpawner.func_230253_a_(world, hostile, peaceful);
        patrolSpawner.func_230253_a_(world, hostile, peaceful);
        catSpawner.func_230253_a_(world, hostile, peaceful);
    }

    @Override
    public List<MobSpawnInfo.Spawners> getSpawns(Biome biome, StructureManager structures, EntityClassification type, BlockPos pos) {
        if (structures.func_235010_a_(pos, true, Structure.field_236374_j_).isValid()) {
            if (type == EntityClassification.MONSTER) {
                return Structure.field_236374_j_.getSpawnList();
            }

            if (type == EntityClassification.CREATURE) {
                return Structure.field_236374_j_.getCreatureSpawnList();
            }
        }

        if (type == EntityClassification.MONSTER) {
            if (structures.func_235010_a_(pos, false, Structure.field_236366_b_).isValid()) {
                return Structure.field_236366_b_.getSpawnList();
            }

            if (structures.func_235010_a_(pos, false, Structure.field_236376_l_).isValid()) {
                return Structure.field_236376_l_.getSpawnList();
            }

            if (structures.func_235010_a_(pos, true, Structure.field_236378_n_).isValid()) {
                return Structure.field_236378_n_.getSpawnList();
            }
        }

        return biome.func_242433_b().func_242559_a(type);
    }

    @SubscribeEvent
    public static void tick(TickEvent.WorldTickEvent event) {
        if (event.phase == TickEvent.Phase.START && event.side.isServer()) {
            mobSpawning = event.world.getGameRules().get(GameRules.DO_MOB_SPAWNING).get();
        }
    }
}
