package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureRegionZoom implements Future<Region>, Callable<Region> {

    private final float cx;
    private final float cz;
    private final float zoom;
    private final boolean filters;
    private final RegionGenerator generator;

    private volatile Region result;

    public FutureRegionZoom(float cx, float cz, float zoom, boolean filters, RegionGenerator generator) {
        this.cx = cx;
        this.cz = cz;
        this.zoom = zoom;
        this.filters = filters;
        this.generator = generator;
    }

    @Override
    public Region call() {
        Region region = result;
        if (region == null) {
            region = generator.generateRegion(cx, cz, zoom, filters);
            result = region;
        }
        return region;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return false;
    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public boolean isDone() {
        return result != null;
    }

    @Override
    public Region get() {
        return call();
    }

    @Override
    public Region get(long timeout, TimeUnit unit) {
        return get();
    }
}
