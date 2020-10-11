/*
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

package com.terraforged.api.level;

import com.terraforged.api.level.type.LevelType;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.util.Optional;
import java.util.Properties;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Supplier;

public class LevelHelper {

    public static Optional<DimensionGeneratorSettings> create(Properties properties, DynamicRegistries registries) {
        LevelType levelType = get(properties, "level-type", () -> "", LevelType::tryParse);
        if (levelType == null) {
            return Optional.empty();
        }

        long seed = get(properties, "level-seed", () -> String.valueOf(new Random().nextLong()), LevelHelper::parseLong);
        boolean chest = get(properties, "generate-bonus-chest", () -> "false", LevelHelper::parseBool);
        boolean structures = get(properties, "generate-structures", () -> "false", LevelHelper::parseBool);

        return Optional.ofNullable(levelType.createLevel(seed, structures, chest, registries));
    }

    private static <T> T get(Properties properties, String key, Supplier<String> def, Function<String, T> parser) {
        String value = properties.getProperty(key);
        if (value == null || value.isEmpty()) {
            return parser.apply(def.get());
        }
        return parser.apply(value);
    }

    private static boolean parseBool(String input) {
        return input.equalsIgnoreCase("true");
    }

    private static long parseLong(String input) {
        try {
            return Long.parseLong(input);
        } catch (NumberFormatException e) {
            return input.hashCode();
        }
    }
}
