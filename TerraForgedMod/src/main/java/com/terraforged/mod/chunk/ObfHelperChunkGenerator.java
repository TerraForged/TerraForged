/*
 *
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

package com.terraforged.mod.chunk;

import com.terraforged.mod.chunk.fix.SpawnFix;
import com.terraforged.mod.util.annotation.Name;
import com.terraforged.mod.util.annotation.Ref;
import net.minecraft.entity.EntityClassification;
import net.minecraft.util.SharedSeedRandom;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.INoiseGenerator;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.OverworldChunkGenerator;
import net.minecraft.world.gen.PerlinNoiseGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.spawner.CatSpawner;
import net.minecraft.world.spawner.PatrolSpawner;
import net.minecraft.world.spawner.PhantomSpawner;
import net.minecraft.world.spawner.WorldEntitySpawner;

import java.util.List;

@Ref({OverworldChunkGenerator.class, NoiseChunkGenerator.class, ChunkGenerator.class})
public abstract class ObfHelperChunkGenerator<T extends GenerationSettings> extends ChunkGenerator<T> {

    private final CatSpawner catSpawner = new CatSpawner();
    private final PatrolSpawner patrolSpawner = new PatrolSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final INoiseGenerator surfaceNoise;

    public ObfHelperChunkGenerator(IWorld world, BiomeProvider biomeProvider, T settings) {
        super(world, biomeProvider, settings);
        SharedSeedRandom random = new SharedSeedRandom(world.getSeed());
        this.surfaceNoise = new PerlinNoiseGenerator(random, 3, 0);
    }

    @Override
    public final void generateStructureStarts(IWorld world, IChunk chunk) {
        try {
            super.generateStructureStarts(world, chunk);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    @Override
    public final void makeBase(IWorld world, IChunk chunk) {
        generateBase(world, chunk);
    }

    @Override
    public final void func_225551_a_(WorldGenRegion world, IChunk chunk) {
        generateSurface(world, chunk);
    }

    @Override
    public final List<Biome.SpawnListEntry> getPossibleCreatures(EntityClassification type, BlockPos pos) {
        if (Feature.SWAMP_HUT.func_202383_b(this.world, pos)) {
            if (type == EntityClassification.MONSTER) {
                return Feature.SWAMP_HUT.getSpawnList();
            }

            if (type == EntityClassification.CREATURE) {
                return Feature.SWAMP_HUT.getCreatureSpawnList();
            }
        } else if (type == EntityClassification.MONSTER) {
            if (Feature.PILLAGER_OUTPOST.isPositionInStructure(this.world, pos)) {
                return Feature.PILLAGER_OUTPOST.getSpawnList();
            }

            if (Feature.OCEAN_MONUMENT.isPositionInStructure(this.world, pos)) {
                return Feature.OCEAN_MONUMENT.getSpawnList();
            }
        }
        return super.getPossibleCreatures(type, pos);
    }

    @Override
    public final void spawnMobs(WorldGenRegion region) {
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

    @Override
    public final void spawnMobs(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        // vanilla does check the mobSpawning gamerule before calling this
        phantomSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        patrolSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        catSpawner.tick(worldIn, spawnHostileMobs, spawnPeacefulMobs);
    }

    @Override
    @Name("getSurfaceLevel")
    public final int func_222529_a(int x, int z, Heightmap.Type type) {
        int level = getTopBlockY(x, z, type) + 1;
        if (type == Heightmap.Type.OCEAN_FLOOR || type == Heightmap.Type.OCEAN_FLOOR_WG) {
            return level;
        }
        return Math.max(getSeaLevel(), level);
    }

    public final double getSurfaceNoise(int x, int z) {
        double scale = 0.0625D;
        double noiseX = x * scale;
        double noiseZ = z * scale;
        double unusedValue1 = scale;
        double unusedValue2 = (x & 15) * scale;
        return surfaceNoise.noiseAt(noiseX, noiseZ, unusedValue1, unusedValue2);
    }

    public abstract int getTopBlockY(int x, int z, Heightmap.Type type);

    public abstract void generateBase(IWorld world, IChunk chunk);

    public abstract void generateSurface(WorldGenRegion world, IChunk chunk);
}
