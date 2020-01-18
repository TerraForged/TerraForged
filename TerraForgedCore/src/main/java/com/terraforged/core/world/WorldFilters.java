package com.terraforged.core.world;

import com.terraforged.core.filter.Erosion;
import com.terraforged.core.filter.Filterable;
import com.terraforged.core.filter.Smoothing;
import com.terraforged.core.filter.Steepness;
import com.terraforged.core.region.Region;
import com.terraforged.core.settings.FilterSettings;
import com.terraforged.core.world.terrain.Terrain;

public class WorldFilters {

    private final Erosion erosion;
    private final Smoothing smoothing;
    private final Steepness steepness;
    private final FilterSettings settings;

    public WorldFilters(GeneratorContext context) {
        context = context.copy();
        this.settings = context.settings.filters;
        this.erosion = new Erosion(context.settings, context.levels);
        this.smoothing = new Smoothing(context.settings, context.levels);
        this.steepness = new Steepness(1, 10F, context.terrain);
    }

    public void apply(Region region) {
        Filterable<Terrain> map = region.filterable();
        erosion.apply(map, region.getRegionX(), region.getRegionZ(), settings.erosion.iterations);
        smoothing.apply(map, region.getRegionX(), region.getRegionZ(), settings.smoothing.iterations);
        steepness.apply(map, region.getRegionX(), region.getRegionZ(), 1);
    }
}
