package com.terraforged.mod.worldgen.terrain;

import com.terraforged.mod.util.ObjectPool;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.ChunkPos;

public class TerrainGenerator {
    protected final TerrainLevels levels;
    protected final NoiseGenerator noiseGenerator;
    protected final ObjectPool<TerrainData> terrainDataPool;

    public TerrainGenerator(long seed, RegistryAccess access) {
        this(TerrainLevels.DEFAULT, new NoiseGenerator(seed, access));
    }

    private TerrainGenerator(TerrainLevels levels, NoiseGenerator noiseGenerator) {
        this.levels = levels;
        this.noiseGenerator = noiseGenerator;
        this.terrainDataPool = new ObjectPool<>(() -> new TerrainData(this.levels));
    }

    public TerrainData generate(ChunkPos chunkPos) {
        var noiseData = noiseGenerator.generate(chunkPos);
        var terrainData = terrainDataPool.take();
        terrainData.assign(noiseData);
        return terrainData;
    }

    public void restore(TerrainData terrainData) {
        terrainDataPool.restore(terrainData);
    }

    public TerrainGenerator withGenerator(long seed, Generator generator) {
        int seaLevel = generator.getSeaLevel();
        int height = generator.getGenDepth();
        var levels = new TerrainLevels(seaLevel, height);
        var noise = noiseGenerator.with(seed, levels);
        return new TerrainGenerator(levels, noise);
    }

    public int getHeight(int x, int z) {
        float heightNoise = noiseGenerator.getHeightNoise(x, z);
        float scaledHeight = levels.getScaledHeight(heightNoise);
        return levels.getHeight(scaledHeight);
    }
}
