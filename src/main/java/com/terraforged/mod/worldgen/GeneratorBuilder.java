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

package com.terraforged.mod.worldgen;

import com.terraforged.mod.data.ModTerrains;
import com.terraforged.mod.worldgen.biome.BiomeGenerator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.noise.NoiseGenerator;
import com.terraforged.mod.worldgen.terrain.TerrainLevels;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public interface GeneratorBuilder {
    static Generator build(long seed, TerrainLevels levels, RegistryAccess registries) {
        var terrain = ModTerrains.getTerrain(registries);

        var biomeGenerator = new BiomeGenerator(seed, registries);
        var noiseGenerator = new NoiseGenerator(seed, levels, terrain).withErosion();
        var biomeSource = new Source(seed, noiseGenerator, registries);
        var vanillaGen = getVanillaGen(seed, biomeSource, registries);

        return new Generator(seed, levels, vanillaGen, biomeSource, biomeGenerator, noiseGenerator);
    }

    static LevelStem getDefault(RegistryAccess registries) {
        var generator = build(0L, TerrainLevels.DEFAULT.copy(), registries);
        var type = registries.ownedRegistryOrThrow(Registry.DIMENSION_TYPE_REGISTRY);
        return new LevelStem(() -> type.getOrThrow(DimensionType.OVERWORLD_LOCATION), generator);
    }

    static VanillaGen getVanillaGen(long seed, BiomeSource biomes, RegistryAccess access) {
        var parameters = access.registryOrThrow(Registry.NOISE_REGISTRY);
        var settings = access.registryOrThrow(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY);
        return new VanillaGen(seed, biomes, () -> settings.getOrThrow(NoiseGeneratorSettings.OVERWORLD), parameters);
    }
}
