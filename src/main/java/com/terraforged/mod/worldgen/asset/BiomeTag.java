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
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.terraforged.mod.registry.ModRegistry;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.resources.RegistryFileCodec;
import net.minecraft.world.level.biome.Biome;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

public class BiomeTag {
    public static final BiomeTag NONE = new BiomeTag(Collections.emptySet());
    public static final Codec<BiomeTag> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Biome.LIST_CODEC.fieldOf("biomes").xmap(BiomeTag::unwrap, BiomeTag::wrap).forGetter(BiomeTag::biomes)
    ).apply(instance, BiomeTag::new));
    public static final Codec<Supplier<BiomeTag>> REFERENCE_CODEC = RegistryFileCodec.create(ModRegistry.BIOME_TAG, CODEC);

    private final Set<Biome> biomes;

    public BiomeTag(Set<Biome> biomes) {
        this.biomes = biomes;
    }

    public Set<Biome> biomes() {
        return biomes;
    }

    public boolean contains(Biome biome) {
        return biomes.contains(biome);
    }

    private static Set<Biome> unwrap(List<Supplier<Biome>> list) {
        var set = new ObjectOpenHashSet<Biome>(list.size());
        for (var biome : list) {
            set.add(biome.get());
        }
        return set;
    }

    private static List<Supplier<Biome>> wrap(Set<Biome> biomes) {
        var list = new ArrayList<Supplier<Biome>>(biomes.size());
        for (var biome : biomes) {
            list.add(() -> biome);
        }
        return list;
    }
}
