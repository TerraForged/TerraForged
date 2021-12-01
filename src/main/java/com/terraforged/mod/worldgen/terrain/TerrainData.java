package com.terraforged.mod.worldgen.terrain;

import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.map.FloatMap;
import com.terraforged.mod.util.map.ObjectMap;
import com.terraforged.mod.worldgen.noise.NoiseData;
import com.terraforged.noise.util.NoiseUtil;

public class TerrainData {
    protected final TerrainLevels levels;
    protected final FloatMap height = new FloatMap();
    protected final FloatMap gradient = new FloatMap();
    protected final ObjectMap<Terrain> terrain = new ObjectMap<>(Terrain[]::new);

    protected float max = 0F;

    public TerrainData(TerrainLevels levels) {
        this.levels = levels;
    }

    public int getMax() {
        return levels.getHeight(max);
    }

    public int getHeight(int x, int z) {
        float scaledHeight = height.get(x, z);
        return levels.getHeight(scaledHeight);
    }

    public TerrainLevels getLevels() {
        return levels;
    }

    public FloatMap getHeight() {
        return height;
    }

    public FloatMap getGradient() {
        return gradient;
    }

    public ObjectMap<Terrain> getTerrain() {
        return terrain;
    }

    public float getGradient(int x, int z, float norm) {
        float grad = getGradient().get(x, z);
        return NoiseUtil.clamp(grad * norm, 0, 1);
    }

    public void assign(NoiseData noiseData) {
        var noiseMap = noiseData.getHeight();
        var terrainMap = noiseData.getTerrain();

        for (int z = 0; z < 16; z++) {
            for (int x = 0; x < 16; x++) {
                float heightNoise = noiseMap.get(x, z);
                float scaledHeight = levels.getScaledHeight(heightNoise);
                var terrainType = terrainMap.get(x, z);

                float n = noiseMap.get(x, z - 1);
                float s = noiseMap.get(x, z + 1);
                float e = noiseMap.get(x + 1, z);
                float w = noiseMap.get(x - 1, z);

                float dx = e - w;
                float dz = s - n;
                float grad = NoiseUtil.sqrt(dx * dx + dz * dz);
                float noiseGrad = NoiseUtil.clamp(grad, 0, 1);

                gradient.set(x, z, noiseGrad);
                terrain.set(x, z, terrainType);
                height.set(x, z, scaledHeight);
                max = Math.max(max, scaledHeight);
            }
        }
    }

    public static boolean isInsideChunk(int x, int z) {
        return x >= 0 && x <= 15 && z >= 0 && z <= 15;
    }
}
