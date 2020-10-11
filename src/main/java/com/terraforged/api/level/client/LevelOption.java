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

package com.terraforged.api.level.client;

import com.terraforged.api.level.type.LevelType;
import net.minecraft.client.gui.screen.BiomeGeneratorTypeScreens;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.DimensionSettings;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Optional;

public class LevelOption extends BiomeGeneratorTypeScreens {

    static {
        BiomeGeneratorTypeScreens.field_239068_c_ = Collections.synchronizedList(new ArrayList<>(BiomeGeneratorTypeScreens.field_239068_c_));
        BiomeGeneratorTypeScreens.field_239069_d_ = Collections.synchronizedMap(new HashMap<>(BiomeGeneratorTypeScreens.field_239069_d_));
    }

    private final LevelType type;
    private final IFactory editScreen;

    protected LevelOption(LevelType type, IFactory editScreen) {
        super(type.getRegistryName().getPath());
        this.type = type;
        this.editScreen = editScreen;
    }

    @Override
    public DimensionGeneratorSettings func_241220_a_(DynamicRegistries.Impl registries, long seed, boolean structures, boolean chast) {
        return type.createLevel(seed, structures, chast, registries);
    }

    @Override
    protected ChunkGenerator func_241869_a(Registry<Biome> biomes, Registry<DimensionSettings> settings, long seed) {
        return type.create(seed, biomes, settings);
    }

    static void register(LevelOption option) {
        BiomeGeneratorTypeScreens.field_239068_c_.add(option);

        if (option.editScreen != null) {
            BiomeGeneratorTypeScreens.field_239069_d_.put(Optional.of(option), option.editScreen);
        }
    }
}
