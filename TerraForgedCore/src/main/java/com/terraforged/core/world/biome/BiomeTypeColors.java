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

package com.terraforged.core.world.biome;

import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BiomeTypeColors {

    private static BiomeTypeColors instance = new BiomeTypeColors();

    private final Map<String, Color> colors = new HashMap<>();

    private BiomeTypeColors() {
        try (InputStream inputStream = BiomeType.class.getResourceAsStream("/biomes.txt")) {
            Properties properties = new Properties();
            properties.load(inputStream);
            for (Map.Entry<?, ?> entry : properties.entrySet()) {
                Color color = Color.decode("#" + entry.getValue().toString());
                colors.put(entry.getKey().toString(), color);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Color getColor(String name, Color defaultColor) {
        return colors.getOrDefault(name, defaultColor);
    }

    public static BiomeTypeColors getInstance() {
        return instance;
    }

    public static void main(String[] args) throws Throwable {
        try (FileWriter writer = new FileWriter("biome_colors.properties")) {
            Properties properties = new Properties();
            for (BiomeType type : BiomeType.values()) {
                int r = type.getColor().getRed();
                int g = type.getColor().getGreen();
                int b = type.getColor().getBlue();
                properties.setProperty(type.name(), String.format("%02x%02x%02x", r, g, b));
            }
            properties.store(writer, "TerraForged BiomeType Hex Colors (do not include hash/pound character)");
        }
    }
}
