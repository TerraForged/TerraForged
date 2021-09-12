/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.feature.sapling;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.terraforged.mod.feature.BlockDataConfig;
import com.terraforged.mod.featuremanager.template.feature.TemplateFeatureConfig;
import com.terraforged.mod.featuremanager.template.template.TemplateManager;
import com.terraforged.mod.featuremanager.util.Json;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

public class SaplingConfig implements BlockDataConfig {

    private final Block block;
    private final List<TemplateFeatureConfig> normal;
    private final List<TemplateFeatureConfig> giant;

    private SaplingConfig(Block block, List<TemplateFeatureConfig> normal, List<TemplateFeatureConfig> giant) {
        this.block = block;
        this.normal = normal;
        this.giant = giant;
    }

    @Override
    public Block getTarget() {
        return block;
    }

    public boolean hasNormal() {
        return !normal.isEmpty();
    }

    public boolean hasGiant() {
        return !giant.isEmpty();
    }

    public TemplateFeatureConfig next(Random random, boolean giant) {
        if (giant) {
            TemplateFeatureConfig config = nextGiant(random);
            if (config != null) {
                return config;
            }
        }
        return nextNormal(random);
    }

    public TemplateFeatureConfig nextNormal(Random random) {
        if (normal.isEmpty()) {
            return null;
        }
        return normal.get(random.nextInt(normal.size()));
    }

    public TemplateFeatureConfig nextGiant(Random random) {
        if (giant.isEmpty()) {
            return null;
        }
        return giant.get(random.nextInt(giant.size()));
    }

    public static Optional<SaplingConfig> deserialize(JsonObject data) {
        String sapling = Json.getString("sapling", data, "");
        Block block = ForgeRegistries.BLOCKS.getValue(new ResourceLocation(sapling));
        if (block == Blocks.AIR) {
            return Optional.empty();
        }
        List<TemplateFeatureConfig> normal = deserializeList(data.getAsJsonObject("normal"));
        List<TemplateFeatureConfig> giant = deserializeList(data.getAsJsonObject("giant"));
        if (normal.isEmpty() && giant.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new SaplingConfig(block, normal, giant));
    }

    private static List<TemplateFeatureConfig> deserializeList(JsonObject object) {
        List<TemplateFeatureConfig> list = Collections.emptyList();
        if (object != null) {
            for (Map.Entry<String, JsonElement> e : object.entrySet()) {
                ResourceLocation name = new ResourceLocation(e.getKey());
                TemplateFeatureConfig template = TemplateManager.getInstance().getTemplateConfig(name);
                if (template != TemplateFeatureConfig.NONE) {
                    int weight = Json.getInt("weight", e.getValue().getAsJsonObject(), 1);
                    for (int i = 0; i < weight; i++) {
                        if (list.isEmpty()) {
                            list = new ArrayList<>();
                        }
                        list.add(template);
                    }
                }
            }
        }
        return list;
    }
}
