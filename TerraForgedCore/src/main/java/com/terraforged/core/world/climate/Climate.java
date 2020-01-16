package com.terraforged.core.world.climate;

import me.dags.noise.Module;
import me.dags.noise.Source;
import me.dags.noise.func.CellFunc;
import me.dags.noise.func.DistanceFunc;
import me.dags.noise.source.Rand;
import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.module.CellLookup;
import com.terraforged.core.module.CellLookupOffset;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.WorldHeightmap;
import com.terraforged.core.world.terrain.Terrain;

public class Climate {

    private final float seaLevel;
    private final float lowerHeight;
    private final float midHeight = 0.425F;
    private final float upperHeight = 0.75F;

    private final float moistureModifier = 0.05F;
    private final float temperatureModifier = 0.175F;

    private final Rand rand;
    private final Module treeLine;
    private final Module heightMap;
    private final Module offsetHeightMap;
    private final Module offsetX;
    private final Module offsetY;

    private final ClimateModule biomeNoise;

    public Climate(GeneratorContext context, WorldHeightmap heightmap) {
        final int cellSeed = context.seed.next();
        final int cellSize = context.settings.generator.biome.biomeSize;
        final int warpScale = context.settings.generator.biome.biomeWarpScale;
        final int warpStrength = context.settings.generator.biome.biomeWarpStrength;

        this.biomeNoise = new ClimateModule(context.seed, context.settings.generator);

        Module warpX = Source.perlin(context.seed.next(), warpScale, 2);
        Module warpZ = Source.perlin(context.seed.next(), warpScale, 2);

        Module windDirection = Source.cubic(context.seed.next(), cellSize, 1);
        Module windStrength = Source.perlin(context.seed.next(), cellSize, 1)
                .scale(cellSize * 1.5)
                .bias(cellSize * 0.5);

        this.treeLine = Source.perlin(context.seed.next(), context.settings.generator.biome.biomeSize * 2, 1)
                .scale(0.1).bias(0.4);

        this.heightMap = Source.build(cellSeed, cellSize, 1)
                .distFunc(DistanceFunc.NATURAL)
                .cellFunc(CellFunc.NOISE_LOOKUP)
                .source(new CellLookup(heightmap::getValue, cellSize))
                .cell()
                .warp(warpX, warpZ, warpStrength);

        this.offsetHeightMap = Source.build(cellSeed, cellSize, 1)
                .distFunc(DistanceFunc.NATURAL)
                .cellFunc(CellFunc.NOISE_LOOKUP)
                .source(new CellLookupOffset(heightmap::getValue, windDirection, windStrength, cellSize))
                .cell()
                .warp(warpX, warpZ, warpStrength);

        this.rand = new Rand(Source.builder().seed(context.seed.next()));
        this.offsetX = context.settings.generator.biomeEdgeNoise.build(context.seed.next());
        this.offsetY = context.settings.generator.biomeEdgeNoise.build(context.seed.next());
        this.seaLevel = context.levels.water;
        this.lowerHeight = context.levels.ground;
    }

    public Rand getRand() {
        return rand;
    }

    public float getCellHeight(float x, float z) {
        return heightMap.getValue(x, z);
    }

    public float getOffsetCellHeight(float x, float z) {
        return offsetHeightMap.getValue(x, z);
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
        modify(cell, x, z);
    }

    private void modify(Cell<Terrain> cell, float x, float z) {
        float height = getCellHeight(x, z);
        float height1 = getOffsetCellHeight(x, z);
        float moistDelta = 0F;
        if (height > seaLevel) {
            if (height1 < seaLevel) {
                moistDelta = 0.7F;
            } else if (height - height1 > 0.2) {
                moistDelta = height - height1;
            } else if (height1 - height > 0.1) {
                moistDelta = Math.max(-0.5F, height - height1) * 2F;
            }
        }

        float moistChange = moistureModifier * moistDelta;
        cell.moisture = NoiseUtil.clamp(cell.moisture + moistChange, 0, 1);

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
