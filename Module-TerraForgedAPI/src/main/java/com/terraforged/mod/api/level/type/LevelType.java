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

package com.terraforged.mod.api.level.type;

import com.mojang.serialization.Codec;
import com.terraforged.mod.api.registry.Registries;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.SimpleRegistry;
import net.minecraft.world.Dimension;
import net.minecraft.world.DimensionType;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;
import net.minecraftforge.registries.IForgeRegistryEntry;

import javax.annotation.Nullable;
import java.util.Locale;

public interface LevelType extends IForgeRegistryEntry<LevelType> {

    Codec<LevelType> CODEC = Registries.LEVEL_TYPES.getCodec();

    ChunkGenerator create(long seed, Registry<Biome> biomes, Registry<DimensionSettings> settings);

    DimensionGeneratorSettings createLevel(long seed, boolean structures, boolean chest, SimpleRegistry<Dimension> dimensions);

    default DimensionGeneratorSettings createLevel(long seed, boolean structures, boolean chest, DynamicRegistries registries) {
        Registry<Biome> biomes = registries.getRegistry(Registry.BIOME_KEY);
        Registry<DimensionType> types = registries.getRegistry(Registry.DIMENSION_TYPE_KEY);
        Registry<DimensionSettings> settings = registries.getRegistry(Registry.NOISE_SETTINGS_KEY);
        LevelBuilder builder = new LevelBuilder(this, seed, structures, chest, types, biomes, settings);
        addDimensions(builder);
        return builder.build();
    }

    default void addDimensions(LevelBuilder builder) {
        ChunkGenerator overworld = create(builder.getSeed(), builder.getBiomes(), builder.getSettings());
        Dimension dimension = new Dimension(builder.getType(DimensionType.OVERWORLD), overworld);
        builder.add(Dimension.OVERWORLD, dimension);
    }

    @Override
    default Class<LevelType> getRegistryType() {
        return LevelType.class;
    }

    @Nullable
    static LevelType tryParse(String name) {
        ResourceLocation location = ResourceLocation.tryCreate(name.toLowerCase(Locale.ROOT));
        if (location == null) {
            return null;
        }

        LevelType levelType = Registries.LEVEL_TYPES.getValue(location);
        if (levelType != null) {
            return levelType;
        }

        LevelType candidate = null;
        for (LevelType type : Registries.LEVEL_TYPES) {
            if (type.getRegistryName().getPath().equalsIgnoreCase(name)) {
                if (candidate != null) {
                    return null;
                }
                candidate = type;
            }
        }

        return candidate;
    }
}
