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

package com.terraforged.mod.worldgen.asset;

import com.mojang.serialization.Codec;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.util.WorldgenTag;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Supplier;

public class BiomeTag extends WorldgenTag<Biome> {
    public static final BiomeTag NONE = new BiomeTag(ObjectSets.emptySet());
    public static final Codec<BiomeTag> DIRECT_CODEC = WorldgenTag.codec("biomes", Biome.LIST_CODEC, BiomeTag::new);
    public static final Codec<Supplier<BiomeTag>> CODEC = RegistryFileCodec.create(ModRegistry.BIOME_TAG, DIRECT_CODEC);

    BiomeTag(ObjectSet<Biome> biomes) {
        super(biomes);
    }
}
