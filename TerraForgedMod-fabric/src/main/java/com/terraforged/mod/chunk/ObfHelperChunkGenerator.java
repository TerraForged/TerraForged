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

import net.minecraft.entity.EntityCategory;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.noise.NoiseSampler;
import net.minecraft.util.math.noise.OctavePerlinNoiseSampler;
import net.minecraft.village.ZombieSiegeManager;
import net.minecraft.world.ChunkRegion;
import net.minecraft.world.Heightmap;
import net.minecraft.world.IWorld;
import net.minecraft.world.SpawnHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.source.BiomeSource;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.CatSpawner;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.gen.PhantomSpawner;
import net.minecraft.world.gen.PillagerSpawner;
import net.minecraft.world.gen.chunk.ChunkGenerator;
import net.minecraft.world.gen.chunk.ChunkGeneratorConfig;
import net.minecraft.world.gen.feature.Feature;

import java.util.List;

public abstract class ObfHelperChunkGenerator<T extends ChunkGeneratorConfig> extends ChunkGenerator<T> {

    private final CatSpawner catSpawner = new CatSpawner();
    private final PillagerSpawner patrolSpawner = new PillagerSpawner();
    private final PhantomSpawner phantomSpawner = new PhantomSpawner();
    private final ZombieSiegeManager zombieSiegeManager = new ZombieSiegeManager();
    private final NoiseSampler surfaceNoise;

    public ObfHelperChunkGenerator(IWorld world, BiomeSource biomeSource, T settings) {
        super(world, biomeSource, settings);
        ChunkRandom random = new ChunkRandom(world.getSeed());
        this.surfaceNoise = new OctavePerlinNoiseSampler(random, 3, 0);
    }

    @Override
    public List<Biome.SpawnEntry> getEntitySpawnList(EntityCategory category, BlockPos pos) {
        if (Feature.SWAMP_HUT.method_14029(this.world, pos)) {
            if (category == EntityCategory.MONSTER) {
                return Feature.SWAMP_HUT.getMonsterSpawns();
            }

            if (category == EntityCategory.CREATURE) {
                return Feature.SWAMP_HUT.getCreatureSpawns();
            }
        } else if (category == EntityCategory.MONSTER) {
            if (Feature.PILLAGER_OUTPOST.isInsideStructure(this.world, pos)) {
                return Feature.PILLAGER_OUTPOST.getMonsterSpawns();
            }

            if (Feature.OCEAN_MONUMENT.isInsideStructure(this.world, pos)) {
                return Feature.OCEAN_MONUMENT.getMonsterSpawns();
            }
        }
        return super.getEntitySpawnList(category, pos);
    }

    @Override
    public void spawnEntities(ServerWorld worldIn, boolean spawnHostileMobs, boolean spawnPeacefulMobs) {
        phantomSpawner.spawn(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        patrolSpawner.spawn(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        catSpawner.spawn(worldIn, spawnHostileMobs, spawnPeacefulMobs);
        zombieSiegeManager.spawn(worldIn, spawnHostileMobs, spawnPeacefulMobs);
    }

    @Override
    public void populateEntities(ChunkRegion region) {
        int chunkX = region.getCenterChunkX();
        int chunkZ = region.getCenterChunkZ();
        Biome biome = region.getChunk(chunkX, chunkZ).getBiomeArray().getBiomeForNoiseGen(0, 0, 0);
        ChunkRandom random = new ChunkRandom();
        random.setSeed(region.getSeed(), chunkX << 4, chunkZ << 4);
        SpawnHelper.populateEntities(region, biome, chunkX, chunkZ, random);
    }

    @Override
    public final int getHeightInGround(int x, int z, Heightmap.Type type) {
        int level = sampleHeight(x, z, type) + 1;
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
        return surfaceNoise.sample(noiseX, noiseZ, unusedValue1, unusedValue2);
    }

    public abstract int sampleHeight(int x, int z, Heightmap.Type type);

    public abstract void populateNoise(IWorld world, Chunk chunk);

    public abstract void buildSurface(ChunkRegion world, Chunk chunk);
}
