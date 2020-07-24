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
import com.terraforged.api.material.WGTags;
import net.minecraft.block.Block;
import net.minecraft.util.ResourceLocation;

import java.io.File;
import java.util.Collection;

public class WorldGenBlocks extends DataGen {

    public static void genBlockTags(File dataDir) {
        if (dataDir.exists() || dataDir.mkdirs()) {
            printMaterials(dataDir, "forge:wg_stone", WGTags.STONE.getAllElements());
            printMaterials(dataDir, "forge:wg_dirt", WGTags.DIRT.getAllElements());
            printMaterials(dataDir, "forge:wg_clay", WGTags.CLAY.getAllElements());
            printMaterials(dataDir, "forge:wg_sediment", WGTags.SEDIMENT.getAllElements());
            printMaterials(dataDir, "forge:wg_erodible", WGTags.ERODIBLE.getAllElements());
        }
    }

    private static void printMaterials(File dir, String name, Collection<Block> blocks) {
        String path = getJsonPath("tags/blocks", new ResourceLocation(name));
        write(new File(dir, path), writer -> {
            JsonObject root = new JsonObject();
            JsonArray values = new JsonArray();
            root.addProperty("replace", false);
            root.add("values", values);
            for (Block block : blocks) {
                values.add("" + block.getRegistryName());
            }
            write(root, writer);
        });
    }
}
