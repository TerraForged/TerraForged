package com.terraforged.app;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.region.Region;
import com.terraforged.core.region.RegionGenerator;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.util.concurrent.ThreadPool;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.WorldGeneratorFactory;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;

public class Cache {

    private float offsetX = 0;
    private float offsetZ = 0;
    private float zoom = 0F;
    private boolean filter = true;
    private Terrains terrain;
    private Settings settings;
    private GeneratorContext context;
    private Region region;
    private RegionGenerator renderer;

    public Cache(int seed) {
        Settings settings = new Settings();
        settings.generator.seed = seed;
        this.settings = settings;
        this.terrain = Terrains.create(settings);
        this.context = new GeneratorContext(terrain, settings);
        this.renderer = RegionGenerator.builder()
                .factory(new WorldGeneratorFactory(context))
                .pool(ThreadPool.getCommon())
                .size(3, 0)
                .build();
    }

    public Settings getSettings() {
        return settings;
    }

    public Terrains getTerrain() {
        return terrain;
    }

    public Terrain getCenterTerrain() {
        Terrain tag = getCenterCell().tag;
        return tag == null ? terrain.ocean : tag;
    }

    public BiomeType getCenterBiomeType() {
        return getCenterCell().biomeType;
    }

    public int getCenterHeight() {
        return (int) (context.levels.worldHeight * getCenterCell().value);
    }

    public Cell<Terrain> getCenterCell() {
        int center = region.getBlockSize().size / 2;
        return region.getCell(center, center);
    }

    public Region getRegion() {
        return region;
    }

    public void update(float offsetX, float offsetZ, float zoom, boolean filters) {
        if (region == null) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.offsetX != offsetX || this.offsetZ != offsetZ) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.zoom != zoom) {
            record(offsetX, offsetZ, zoom, filters);
            return;
        }
        if (this.filter != filters) {
            record(offsetX, offsetZ, zoom, filters);
        }
    }

    private void record(float offsetX, float offsetZ, float zoom, boolean filters) {
        this.zoom = zoom;
        this.filter = filters;
        this.offsetX = offsetX;
        this.offsetZ = offsetZ;
        try {
            this.region = renderer.generateRegion(offsetX, offsetZ, zoom, filters);
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }
}
