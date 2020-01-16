package com.terraforged.core.world;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.filter.Erosion;
import com.terraforged.core.filter.Smoothing;
import com.terraforged.core.filter.Steepness;
import com.terraforged.core.settings.FilterSettings;

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

    public void setRegion(int regionX, int regionZ) {
        long seed = NoiseUtil.seed(regionX, regionZ);
        getErosion().setSeed(seed);
        getSmoothing().setSeed(seed);
        getSteepness().setSeed(seed);
    }

    public FilterSettings getSettings() {
        return settings;
    }

    public Erosion getErosion() {
        return erosion;
    }

    public Smoothing getSmoothing() {
        return smoothing;
    }

    public Steepness getSteepness() {
        return steepness;
    }
}
