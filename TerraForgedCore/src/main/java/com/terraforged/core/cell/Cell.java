package com.terraforged.core.cell;

import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.util.NoiseUtil;

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
    public float region;
    public float regionEdge;
    public float biome;
    public float biomeEdge = 1F;
    public float riverMask = 1F;

    public float value;
    public float biomeMoisture;
    public float biomeTemperature;
    public float moisture;
    public float temperature;
    public float steepness;
    public float erosion;
    public float sediment;
    public float biomeTypeMask = 1F;
    public BiomeType biomeType = BiomeType.GRASSLAND;

    public T tag = null;

    public void copy(Cell<T> other) {
        value = other.value;

        continent = other.continent;
        continentEdge = other.continentEdge;

        region = other.region;
        regionEdge = other.regionEdge;

        biome = other.biome;
        biomeEdge = other.biomeEdge;

        riverMask = other.riverMask;

        moisture = other.moisture;
        temperature = other.temperature;
        biomeMoisture = other.biomeMoisture;
        biomeTemperature = other.biomeTemperature;

        steepness = other.steepness;
        erosion = other.erosion;
        sediment = other.sediment;
        biomeType = other.biomeType;
        biomeTypeMask = other.biomeTypeMask;

        tag = other.tag;
    }

    public float continentMask(float min, float max) {
        return NoiseUtil.map(continentEdge, min, max, max - min);
    }

    public float regionMask(float min, float max) {
        return NoiseUtil.map(regionEdge, min, max, max - min);
    }

    public float biomeMask(float min, float max) {
        return NoiseUtil.map(biomeEdge, min, max, max - min);
    }

    public float mask(float cmin, float cmax, float rmin, float rmax) {
        return riverMask * continentMask(cmin, cmax) * regionMask(rmin, rmax);
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
