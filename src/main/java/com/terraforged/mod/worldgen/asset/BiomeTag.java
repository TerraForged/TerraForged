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
import com.terraforged.mod.codec.LazyCodec;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.worldgen.util.WorldgenTag;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import it.unimi.dsi.fastutil.objects.ObjectSet;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

import java.util.function.Supplier;

public class BiomeTag extends WorldgenTag<Biome> {
    public static final BiomeTag NONE = new BiomeTag(ObjectSets.emptySet());
    public static final Codec<BiomeTag> DIRECT = WorldgenTag.codec("biomes", () -> Biome.LIST_CODEC, BiomeTag::new);
    public static final Codec<Supplier<BiomeTag>> CODEC = LazyCodec.registry(DIRECT, ModRegistry.BIOME_TAG);

    public BiomeTag(ObjectSet<Biome> biomes) {
        super(biomes);
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @SafeVarargs
    public static BiomeTag of(Registry<Biome> registry, ResourceKey<Biome>... biomes) {
        var set = new ObjectOpenHashSet<Biome>();
        for (var key : biomes) {
            set.add(registry.getOrThrow(key));
        }
        return new BiomeTag(set);
    }

    public static BiomeTag empty() {
        return new BiomeTag(ObjectSets.emptySet());
    }
}
