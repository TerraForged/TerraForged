package com.terraforged.core.world.terrain;

import com.terraforged.core.cell.Tag;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.heightmap.Levels;

import java.util.concurrent.atomic.AtomicInteger;

public class Terrain implements Tag {

    public static final int ID_START = 9;
    public static final AtomicInteger MAX_ID = new AtomicInteger(0);
    public static final Terrain NONE = new Terrain("none", -1);

    private final String name;
    private final int id;
    private float weight;

    public Terrain(String name, int id) {
        this(name, id, 1F);
    }

    public Terrain(String name, int id, double weight) {
        this.name = name;
        this.id = id;
        this.weight = (float) weight;
        MAX_ID.set(Math.max(MAX_ID.get(), id));
    }

    public int getId() {
        return id;
    }

    @Override
    public String getName() {
        return name;
    }

    public float getMax(float noise) {
        return 1F;
    }

    public float getWeight() {
        return weight;
    }

    @Override
    public String toString() {
        return getName();
    }

    public static Terrain ocean(Settings settings) {
        return new Terrain("ocean", 0) {

            private final float max = new Levels(settings.generator).water;

            @Override
            public float getMax(float noise) {
                return max;
            }
        };
    }

    public static Terrain deepOcean(Settings settings) {
        return new Terrain("deep_ocean", 1) {

            private final float max = new Levels(settings.generator).water / 2F;

            @Override
            public float getMax(float noise) {
                return max;
            }
        };
    }

    public static Terrain coast(Settings settings) {
        return new Terrain("coast", 2) {

            private final float max = new Levels(settings.generator).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain beach(Settings settings) {
        return new Terrain("beach", 2) {

            private final float max = new Levels(settings.generator).ground(1);

            @Override
            public float getMax(float noise) {
                return max + (noise * (1 / 255F));
            }
        };
    }

    public static Terrain lake(Settings settings) {
        return new Terrain("lake", 3);
    }

    public static Terrain river(Settings settings) {
        return new Terrain("river", 3);
    }

    public static Terrain riverBank(Settings settings) {
        return new Terrain("river_banks", 4);
    }

    public static Terrain steppe(Settings settings) {
        return new Terrain("steppe", 5, settings.terrain.steppe.weight);
    }

    public static Terrain plains(Settings settings) {
        return new Terrain("plains", 5, settings.terrain.plains.weight);
    }

    public static Terrain plateau(Settings settings) {
        return new Terrain("plateau", 6, settings.terrain.plateau.weight);
    }

    public static Terrain badlands(Settings settings) {
        return new Terrain("badlands", 7, settings.terrain.badlands.weight);
    }

    public static Terrain hills(Settings settings) {
        return new Terrain("hills", 8, settings.terrain.hills.weight);
    }

    public static Terrain dales(Settings settings) {
        return new Terrain("dales", 9, settings.terrain.dales.weight);
    }

    public static Terrain torridonian(Settings settings) {
        return new Terrain("torridonian_fells", 10, settings.terrain.torridonian.weight);
    }

    public static Terrain mountains(Settings settings) {
        return new Terrain("mountains", 11, settings.terrain.mountains.weight);
    }

    public static Terrain volcano(Settings settings) {
        return new Terrain("volcano", 12, settings.terrain.volcano.weight);
    }

    public static Terrain volcanoPipe(Settings settings) {
        return new Terrain("volcano_pipe", 13, settings.terrain.volcano.weight);
    }
}
