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

package com.terraforged.mod.biome.map;

import com.google.common.collect.Sets;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.mod.biome.ModBiomes;
import com.terraforged.mod.biome.provider.BiomeHelper;
import me.dags.noise.util.NoiseUtil;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class AbstractBiomeMap implements BiomeMap {

    private final Biome[][] beach;
    private final Biome[][] river;
    private final Biome[][] ocean;
    private final Biome[][] deepOcean;

    protected AbstractBiomeMap(BiomeMapBuilder builder) {
        river = builder.rivers();
        beach = builder.beaches();
        ocean = builder.oceans();
        deepOcean = builder.deepOceans();
    }

    @Override
    public Biome getBeach(float temperature, float moisture, float shape) {
        return get(beach, getCategory(temperature), shape, defaultBeach(temperature));
    }

    @Override
    public Biome getRiver(float temperature, float moisture, float shape) {
        return get(river, getCategory(temperature), shape, defaultRiver(temperature));
    }

    @Override
    public Biome getOcean(float temperature, float moisture, float shape) {
        return get(ocean, getCategory(temperature), shape, defaultOcean(temperature));
    }

    @Override
    public Biome getDeepOcean(float temperature, float moisture, float shape) {
        return get(deepOcean, getCategory(temperature), shape, defaultDeepOcean(temperature));
    }

    @Override
    public Set<Biome> getOceanBiomes(Biome.TempCategory temp) {
        return Sets.newHashSet(ocean[temp.ordinal() - 1]);
    }

    @Override
    public Set<Biome> getDeepOceanBiomes(Biome.TempCategory temp) {
        return Sets.newHashSet(deepOcean[temp.ordinal() - 1]);
    }

    @Override
    public Set<Biome> getRivers(Biome.TempCategory temp) {
        return Sets.newHashSet(river[temp.ordinal() - 1]);
    }

    @Override
    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.add("rivers", collect(river));
        root.add("beaches", collect(river));
        root.add("oceans", collect(ocean));
        root.add("deepOceans", collect(deepOcean));
        return root;
    }

    private JsonObject collect(Biome[][] biomes) {
        JsonObject root = new JsonObject();
        for (Biome.TempCategory temp : Biome.TempCategory.values()) {
            if (temp == Biome.TempCategory.OCEAN) {
                continue;
            }
            JsonArray array = new JsonArray();
            Biome[] group = biomes[temp.ordinal() - 1];
            if (group != null) {
                Set<Biome> set = new HashSet<>();
                Collections.addAll(set, group);
                set.stream().map(BiomeHelper::getId).sorted().forEach(array::add);
            }
            root.add(temp.name(), array);
        }
        return root;
    }


    protected Biome.TempCategory getCategory(float value) {
        if (value < 0.25) {
            return Biome.TempCategory.COLD;
        }
        if (value > 0.75) {
            return Biome.TempCategory.WARM;
        }
        return Biome.TempCategory.MEDIUM;
    }

    protected Biome defaultBeach(float temperature) {
        if (temperature < 0.25) {
            return Biomes.SNOWY_BEACH;
        }
        if (temperature > 0.75) {
            return ModBiomes.WARM_BEACH;
        }
        return Biomes.BEACH;
    }

    protected Biome defaultRiver(float temperature) {
        if (temperature < 0.15) {
            return Biomes.FROZEN_RIVER;
        }
        return Biomes.RIVER;
    }

    protected Biome defaultOcean(float temperature) {
        if (temperature < 0.3) {
            return Biomes.FROZEN_OCEAN;
        }
        if (temperature > 0.7) {
            return Biomes.WARM_OCEAN;
        }
        return Biomes.OCEAN;
    }

    protected Biome defaultDeepOcean(float temperature) {
        if (temperature < 0.3) {
            return Biomes.DEEP_FROZEN_OCEAN;
        }
        if (temperature > 0.7) {
            return Biomes.DEEP_WARM_OCEAN;
        }
        return Biomes.DEEP_OCEAN;
    }

    protected Biome defaultBiome(float temperature, float moisture) {
        if (temperature < 0.3) {
            return ModBiomes.TAIGA_SCRUB;
        }
        if (temperature > 0.7) {
            return ModBiomes.SAVANNA_SCRUB;
        }
        return Biomes.PLAINS;
    }

    protected Biome get(Biome[][] group, Biome.TempCategory category, float shape, Biome def) {
        return get(group, category.ordinal() - 1, shape, def);
    }

    protected Biome get(Biome[][] group, BiomeType type, float shape, Biome def) {
        return get(group, type.ordinal(), shape, def);
    }

    protected Biome get(Biome[][] group, int ordinal, float shape, Biome def) {
        if (ordinal >= group.length) {
            return def;
        }

        Biome[] biomes = group[ordinal];
        if (biomes == null || biomes.length == 0) {
            return def;
        }

        int index = NoiseUtil.round((biomes.length - 1) * shape);
        return biomes[index];
    }
}
