package com.terraforged.mod.worldgen.noise;

import com.terraforged.engine.world.terrain.Terrain;
import com.terraforged.mod.util.map.FloatMap;
import com.terraforged.mod.util.map.Index;
import com.terraforged.mod.util.map.ObjectMap;

public class NoiseData {
    protected static final int BORDER = 1;
    protected static final int MIN = -BORDER;
    protected static final int MAX = 16 + BORDER;

    protected final NoiseSample sample = new NoiseSample();
    protected final FloatMap height = new FloatMap(BORDER);
    protected final FloatMap water = new FloatMap(BORDER);
    protected final FloatMap moisture = new FloatMap(BORDER);
    protected final FloatMap temperature = new FloatMap(BORDER);
    protected final ObjectMap<Terrain> terrain = new ObjectMap<>(BORDER, Terrain[]::new);

    public int min() {
        return MIN;
    }

    public int max() {
        return MAX;
    }

    public Index index() {
        return height.index();
    }

    public NoiseSample getSample() {
        return sample;
    }

    public FloatMap getHeight() {
        return height;
    }

    public FloatMap getWater() {
        return water;
    }

    public FloatMap getMoisture() {
        return moisture;
    }

    public FloatMap getTemperature() {
        return temperature;
    }

    public ObjectMap<Terrain> getTerrain() {
        return terrain;
    }

    public void setNoise(int x, int z, NoiseSample sample) {
        int index = index().of(x, z);
        terrain.set(index, sample.terrainType);
        height.set(index, sample.heightNoise);
        water.set(index, sample.riverNoise);
    }
}
