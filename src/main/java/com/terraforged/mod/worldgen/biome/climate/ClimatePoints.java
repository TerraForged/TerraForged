package com.terraforged.mod.worldgen.biome.climate;

import net.minecraft.world.level.biome.Climate;

import java.util.concurrent.atomic.AtomicInteger;

public class ClimatePoints {
    private final AtomicInteger counter = new AtomicInteger(0);

    public Climate.TargetPoint next() {
        return new Climate.TargetPoint(0, 0, 0, 0, 0, counter.getAndIncrement());
    }
}
