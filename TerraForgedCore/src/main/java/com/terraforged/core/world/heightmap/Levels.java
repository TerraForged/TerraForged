package com.terraforged.core.world.heightmap;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.settings.GeneratorSettings;

public class Levels {

    public final int worldHeight;

    // y index of the top-most water block
    public final int waterY;
    private final int groundY;

    // top of the first ground block (ie 1 above ground index)
    public final int groundLevel;
    // top of the top-most water block (ie 1 above water index)
    public final int waterLevel;

    // ground index mapped between 0-1 (default 63 / 256)
    public final float ground;
    // water index mapped between 0-1 (default 62 / 256)
    public final float water;

    public Levels(GeneratorSettings settings) {
        worldHeight = Math.max(1, settings.world.worldHeight);

        waterLevel = settings.world.seaLevel;
        groundLevel = waterLevel + 1;

        waterY = Math.min(waterLevel - 1, worldHeight);
        groundY = Math.min(groundLevel - 1, worldHeight);

        ground = NoiseUtil.div(groundY, worldHeight);
        water = NoiseUtil.div(waterY, worldHeight);
    }

    public float scale(int level) {
        return NoiseUtil.div(level, worldHeight);
    }

    public float water(int amount) {
        return NoiseUtil.div(waterY + amount, worldHeight);
    }

    public float ground(int amount) {
        return NoiseUtil.div(groundY + amount, worldHeight);
    }

    public static float getSeaLevel(GeneratorSettings settings) {
        int worldHeight = Math.max(1, settings.world.worldHeight);
        int waterLevel = Math.min(settings.world.seaLevel, worldHeight);
        return NoiseUtil.div(waterLevel, worldHeight);
    }
}
