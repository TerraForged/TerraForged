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

package com.terraforged.mod.util.seed;

import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.profiler.GeneratorProfiler;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.dimension.LevelStem;

import java.util.OptionalInt;

public class SeedUtil {
    public static Registry<LevelStem> reseed(long seed, Registry<LevelStem> registry) {
        var overworld = registry.getOrThrow(LevelStem.OVERWORLD);
        var generator = overworld.generator();

        if (generator instanceof Generator) {
            generator = generator.withSeed(seed);
            TerraForged.LOG.info("Re-seeded TerraForged generator: {}", seed);
        }

        if (Environment.PROFILING) {
            generator = withProfiler(generator);
        }

        if (generator == overworld.generator()) return registry;

        var lifecycle = registry.lifecycle(overworld);
        var levelStem = new LevelStem(overworld.typeHolder(), generator);
        ((MappedRegistry<LevelStem>) registry).registerOrOverride(OptionalInt.empty(), LevelStem.OVERWORLD, levelStem, lifecycle);

        if (GeneratorProfiler.PROFILING.get()) {
            TerraForged.LOG.info("Attached generator profiler");
        }

        return registry;
    }

    public static ChunkGenerator withProfiler(ChunkGenerator generator) {
        if (GeneratorProfiler.PROFILING.get()) {
//            return GeneratorProfiler.wrap(generator);
        }
        return generator;
    }
}
