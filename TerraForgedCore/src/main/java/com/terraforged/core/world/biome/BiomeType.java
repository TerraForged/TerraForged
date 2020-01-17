package com.terraforged.core.world.biome;

import me.dags.noise.util.NoiseUtil;
import com.terraforged.core.cell.Cell;

import java.awt.*;

public enum BiomeType {

    TROPICAL_RAINFOREST(7, 83, 48, new Color(7, 83, 48)),
    SAVANNA(151, 165, 39, new Color(151, 165, 39)),
    DESERT(200, 113, 55, new Color(200, 113, 55)),
    TEMPERATE_RAINFOREST(10, 84, 109, new Color(10, 160, 65)),
    TEMPERATE_FOREST(44, 137, 160, new Color(50, 200, 80)),
    GRASSLAND(179, 124, 6, new Color(100, 220, 60)),
    COLD_STEPPE(131, 112, 71, new Color(175, 180, 150)),
    STEPPE(199, 155, 60, new Color(200, 200, 120)),
    TAIGA(91, 143, 82, new Color(91, 143, 82)),
    TUNDRA(147, 167, 172, new Color(147, 167, 172)),
    ALPINE(0, 0, 0, new Color(160, 120, 170));

    public static final int RESOLUTION = 256;
    public static final int MAX = RESOLUTION - 1;

    private final Color lookup;
    private final Color color;

    BiomeType(int r, int g, int b, Color color) {
        this(new Color(r, g, b), color);
    }

    BiomeType(Color lookup, Color color) {
        this.lookup = lookup;
        this.color = BiomeTypeColors.getInstance().getColor(name(), color);
    }

    Color getLookup() {
        return lookup;
    }

    public Color getColor() {
        return color;
    }

    public boolean isExtreme() {
        return this == TUNDRA || this == DESERT;
    }

    public static BiomeType get(float temperature, float moisture) {
        return getCurve(temperature, moisture);
    }

    public static BiomeType getLinear(float temperature, float moisture) {
        int x = NoiseUtil.round(MAX * temperature);
        int y = getYLinear(x, temperature, moisture);
        return getType(x, y);
    }

    public static BiomeType getCurve(float temperature, float moisture) {
        int x = NoiseUtil.round(MAX * temperature);
        int y = getYCurve(x, temperature, moisture);
        return getType(x, y);
    }

    public static float getEdge(float temperature, float moisture) {
        return getEdgeCurve(temperature, moisture);
    }

    public static float getEdgeLinear(float temperature, float moisture) {
        int x = NoiseUtil.round(MAX * temperature);
        int y = getYLinear(x, temperature, moisture);
        return getEdge(x, y);
    }

    public static float getEdgeCurve(float temperature, float moisture) {
        int x = NoiseUtil.round(MAX * temperature);
        int y = getYCurve(x, temperature, moisture);
        return getEdge(x, y);
    }

    public static void apply(Cell<?> cell) {
        applyCurve(cell);
    }

    public static void applyLinear(Cell<?> cell) {
        cell.biomeType = get(cell.biomeTemperature, cell.biomeMoisture);
        cell.biomeTypeMask = getEdge(cell.temperature, cell.moisture);
    }

    public static void applyCurve(Cell<?> cell) {
        cell.biomeType = get(cell.biomeTemperature, cell.biomeMoisture);
        cell.biomeTypeMask = getEdge(cell.temperature, cell.moisture);
    }

    private static BiomeType getType(int x, int y) {
        return BiomeTypeLoader.getInstance().getTypeMap()[y][x];
    }

    private static float getEdge(int x, int y) {
        return BiomeTypeLoader.getInstance().getEdgeMap()[y][x];
    }

    private static int getYLinear(int x, float temperature, float moisture) {
        if (moisture > temperature) {
            return x;
        }
        return NoiseUtil.round(MAX * moisture);
    }

    private static int getYCurve(int x, float temperature, float moisture) {
        int max = x + ((MAX - x) / 2);
        int y = NoiseUtil.round(max * moisture);
        return Math.min(x, y);
    }
}
