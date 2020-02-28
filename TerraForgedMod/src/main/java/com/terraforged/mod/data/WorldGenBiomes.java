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

package com.terraforged.mod.data;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.terraforged.core.world.biome.BiomeType;
import com.terraforged.mod.biome.map.BiomeMap;
import com.terraforged.mod.biome.provider.BiomeHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.biome.Biome;

import java.io.File;
import java.util.Set;

public class WorldGenBiomes extends DataGen {

    public static void genBiomeMap(File dataDir) {
        BiomeMap map = BiomeHelper.getDefaultBiomeMap();
        for (BiomeType type : BiomeType.values()) {
            genBiomes(dataDir, type, map);
        }
    }

    private static void genBiomes(File dir, BiomeType type, BiomeMap map) {
        String path = getJsonPath("tags/biomes", getName(type));

        write(new File(dir, path), writer -> {
            Set<Biome> biomes = map.getBiomes(type);
            JsonObject root = new JsonObject();
            JsonArray values = new JsonArray();

            root.addProperty("replaceable", false);
            root.add("values", values);

            biomes.stream()
                    .map(b -> b.getRegistryName() + "")
                    .sorted()
                    .forEach(values::add);

            write(root, writer);
        });
    }

    private static ResourceLocation getName(BiomeType type) {
        return new ResourceLocation("terraforged", type.name().toLowerCase());
    }
}
