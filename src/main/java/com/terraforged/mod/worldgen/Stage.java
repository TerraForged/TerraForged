package com.terraforged.mod.worldgen;

import com.terraforged.mod.util.Timer;

import java.util.concurrent.TimeUnit;

public enum Stage {
    STRUCTURE_STARTS,
    STRUCTURE_REFS,
    BIOMES,
    FILL,
    SURFACE,
    CARVER,
    DECORATION,
    ;

    private final Timer timer;

    Stage() {
        timer = new Timer(name(), 5, TimeUnit.SECONDS);
    }

    public Timer timer() {
        return timer;
    }

    public Timer.Instance start() {
        return timer.start();
    }
}
