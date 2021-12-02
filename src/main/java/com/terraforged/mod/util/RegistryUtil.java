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

package com.terraforged.mod.util;

import com.google.common.base.Suppliers;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.Generator;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.world.level.dimension.LevelStem;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

public class RegistryUtil {
    public static MappedRegistry<LevelStem> reseed(long seed, MappedRegistry<LevelStem> registry) {
        var overworld = registry.getOrThrow(LevelStem.OVERWORLD);

        if (overworld.generator() instanceof Generator) {
            var generator = overworld.generator().withSeed(seed);
            if (generator == overworld.generator()) return registry;

            var lifecycle = registry.lifecycle(overworld);
            var levelStem = new LevelStem(overworld.typeSupplier(), generator);
            registry.registerOrOverride(OptionalInt.empty(), LevelStem.OVERWORLD, levelStem, lifecycle);

            TerraForged.LOG.info("Re-seeded TerraForged generator: {}", seed);
        }

        return registry;
    }

    public static Optional<RegistryAccess> getAccess(DynamicOps<?> ops) {
        if (!(ops instanceof RegistryReadOps)) {
            return Optional.empty();
        }

        try {
            var field = REGISTRY_ACCESS_GETTER.get();
            var access = (RegistryAccess) field.get(ops);
            return Optional.ofNullable(access);
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    protected static final Supplier<Field> REGISTRY_ACCESS_GETTER = Suppliers.memoize(() -> {
        for (var field : RegistryReadOps.class.getDeclaredFields()) {
            if (field.getType() == RegistryAccess.class) {
                field.setAccessible(true);
                return field;
            }
        }

        throw new UnsupportedOperationException("Method not found!");
    });
}
