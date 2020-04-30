package com.terraforged.core.region.gen;

import com.terraforged.core.region.Region;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class FutureRegion implements Future<Region>, Callable<Region> {

    private final int rx;
    private final int rz;
    private final RegionGenerator generator;

    private volatile Region result;

    public FutureRegion(int rx, int rz, RegionGenerator generator) {
        this.rx = rx;
        this.rz = rz;
        this.generator = generator;
    }

    @Override
    public Region call() {
        Region region = result;
        if (region == null) {
            region = generator.generateRegion(rx, rz);
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
