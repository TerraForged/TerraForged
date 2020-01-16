package com.terraforged.core.cell;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;

public class Cell<T extends Tag> {

    private static final Cell EMPTY = new Cell() {
        @Override
        public boolean isAbsent() {
            return true;
        }
    };

    private static final ObjectPool<Cell<Terrain>> POOL = new ObjectPool<>(100, Cell::new);

    public float continent;
    public float continentEdge;

    public float value;
    public float biome;
    public float biomeMoisture;
    public float biomeTemperature;
    public float moisture;
    public float temperature;
    public float steepness;
    public float erosion;
    public float sediment;

    public float mask = 1F;
    public float biomeMask = 1F;
    public float biomeTypeMask = 1F;
    public float regionMask = 1F;
    public float riverMask = 1F;
    public BiomeType biomeType = BiomeType.GRASSLAND;

    public T tag = null;

    public void copy(Cell<T> other) {
        continent = other.continent;
        continentEdge = other.continentEdge;

        value = other.value;
        biome = other.biome;

        biomeMask = other.biomeMask;
        riverMask = other.riverMask;
        biomeMoisture = other.biomeMoisture;
        biomeTemperature = other.biomeTemperature;
        mask = other.mask;
        regionMask = other.regionMask;
        moisture = other.moisture;
        temperature = other.temperature;
        steepness = other.steepness;
        erosion = other.erosion;
        sediment = other.sediment;
        biomeType = other.biomeType;
        biomeTypeMask = other.biomeTypeMask;
        tag = other.tag;
    }

    public float combinedMask(float clamp) {
        return NoiseUtil.map(biomeMask * regionMask, 0, clamp, clamp);
    }

    public float biomeMask(float clamp) {
        return NoiseUtil.map(biomeMask, 0, clamp, clamp);
    }

    public float regionMask(float clamp) {
        return NoiseUtil.map(regionMask, 0, clamp, clamp);
    }

    public boolean isAbsent() {
        return false;
    }

    @SuppressWarnings("unchecked")
    public static <T extends Tag> Cell<T> empty() {
        return EMPTY;
    }

    public static ObjectPool.Item<Cell<Terrain>> pooled() {
        return POOL.get();
    }

    public interface Visitor<T extends Tag> {

        void visit(Cell<T> cell, int dx, int dz);
    }

    public interface ZoomVisitor<T extends Tag> {

        void visit(Cell<T> cell, float x, float z);
    }
}
