/*
 *
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.world.terrain;

import com.terraforged.core.cell.Tag;
import com.terraforged.core.settings.Settings;
import com.terraforged.core.world.heightmap.Levels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class Terrain implements Tag {

    private static final AtomicInteger MAX_ID = new AtomicInteger(0);
    private static final Map<String, Terrain> register = Collections.synchronizedMap(new HashMap<>());

    public static final int ID_START = 10;
    public static final Terrain NONE = new Terrain("none", -1);

    private final String name;
    private final int id;
    private final float weight;

    public Terrain(String name, int id) {
        this(name, id, 1F);
    }

    public Terrain(String name, int id, double weight) {
        this.name = name;
        this.id = id;
        this.weight = (float) weight;
        register.put(name, this);
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

    public float getHue() {
        return id / (float) MAX_ID.get();
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

    public static Terrain wetlands(Settings settings) {
        return new Terrain("wetlands", 5);
    }

    public static Terrain steppe(Settings settings) {
        return new Terrain("steppe", 6, settings.terrain.steppe.weight);
    }

    public static Terrain plains(Settings settings) {
        return new Terrain("plains", 7, settings.terrain.plains.weight);
    }

    public static Terrain plateau(Settings settings) {
        return new Terrain("plateau", 8, settings.terrain.plateau.weight);
    }

    public static Terrain badlands(Settings settings) {
        return new Terrain("badlands", 9, settings.terrain.badlands.weight);
    }

    public static Terrain hills(Settings settings) {
        return new Terrain("hills", 10, settings.terrain.hills.weight);
    }

    public static Terrain dales(Settings settings) {
        return new Terrain("dales", 11, settings.terrain.dales.weight);
    }

    public static Terrain torridonian(Settings settings) {
        return new Terrain("torridonian_fells", 12, settings.terrain.torridonian.weight);
    }

    public static Terrain mountains(Settings settings) {
        return new Terrain("mountains", 13, settings.terrain.mountains.weight);
    }

    public static Terrain volcano(Settings settings) {
        return new Terrain("volcano", 14, settings.terrain.volcano.weight);
    }

    public static Terrain volcanoPipe(Settings settings) {
        return new Terrain("volcano_pipe", 15, settings.terrain.volcano.weight);
    }

    public static Optional<Terrain> get(String name) {
        return Optional.ofNullable(register.get(name));
    }

    public static List<Terrain> getRegistered() {
        return new ArrayList<>(register.values());
    }
}
