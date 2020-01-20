package com.terraforged.core.world.climate;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.core.world.heightmap.WorldHeightmap;
import com.terraforged.core.world.terrain.Terrain;
import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.source.Rand;

public class Climate {

    private final float seaLevel;
    private final float lowerHeight;
    private final float midHeight = 0.45F;
    private final float upperHeight = 0.75F;

    private final float moistureModifier = 0.1F;
    private final float temperatureModifier = 0.05F;

    private final Rand rand;
    private final Module treeLine;
    private final Module offsetX;
    private final Module offsetY;

    private final ClimateModule biomeNoise;

    public Climate(GeneratorContext context, WorldHeightmap heightmap) {
        this.biomeNoise = new ClimateModule(context.seed, context.settings.generator);

        this.treeLine = Source.perlin(context.seed.next(), context.settings.generator.biome.biomeSize * 2, 1)
                .scale(0.1).bias(0.4);

        this.rand = new Rand(Source.builder().seed(context.seed.next()));
        this.offsetX = context.settings.generator.biomeEdgeNoise.build(context.seed.next());
        this.offsetY = context.settings.generator.biomeEdgeNoise.build(context.seed.next());
        this.seaLevel = context.levels.water;
        this.lowerHeight = context.levels.ground;
    }

    public Rand getRand() {
        return rand;
    }

    public float getOffsetX(float x, float z, int distance) {
        return offsetX.getValue(x, z) * distance;
    }

    public float getOffsetZ(float x, float z, int distance) {
        return offsetY.getValue(x, z) * distance;
    }

    public float getTreeLine(float x, float z) {
        return treeLine.getValue(x, z);
    }

    public void apply(Cell<Terrain> cell, float x, float z, boolean mask) {
        biomeNoise.apply(cell, x, z, mask);

        modifyTemp(cell, x, z);
    }

    private void modifyTemp(Cell<Terrain> cell, float x, float z) {
        float height = cell.value;
        if (height > upperHeight) {
            cell.temperature = Math.max(0, cell.temperature - temperatureModifier);
            return;
        }

        // temperature decreases away from 'midHeight' towards 'upperHeight'
        if (height > midHeight) {
            float delta = (height - midHeight) / (upperHeight - midHeight);
            cell.temperature = Math.max(0, cell.temperature - (delta * temperatureModifier));
            return;
        }

        height = Math.max(lowerHeight, height);

        // temperature increases away from 'midHeight' towards 'lowerHeight'
        if (height >= lowerHeight) {
            float delta = 1 - ((height - lowerHeight) / (midHeight - lowerHeight));
            cell.temperature = Math.min(1, cell.temperature + (delta * temperatureModifier));
        }
    }
}
