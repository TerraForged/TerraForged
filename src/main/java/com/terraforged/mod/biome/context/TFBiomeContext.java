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

package com.terraforged.mod.biome.context;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.engine.world.biome.map.BiomeContext;
import com.terraforged.mod.featuremanager.util.RegistryInstance;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;

public class TFBiomeContext implements BiomeContext<RegistryKey<Biome>> {

    public static final Codec<TFBiomeContext> CODEC = Codecs.create(TFBiomeContext::encodeBiomeContext, TFBiomeContext::decodeBiomeContext);

    private final BiomeDefaults defaults;
    private final BiomeProperties properties;
    public final RegistryInstance<Biome> biomes;

    public TFBiomeContext(DynamicRegistries.Impl registries) {
        this(registries.registryOrThrow(Registry.BIOME_REGISTRY));
    }

    public TFBiomeContext(Registry<Biome> biomes) {
        this.biomes = new RegistryInstance<>(biomes);
        this.defaults = new BiomeDefaults(this);
        this.properties = new BiomeProperties(this);
    }

    @Override
    public int getId(RegistryKey<Biome> key) {
        return biomes.getId(key);
    }

    @Override
    public RegistryKey<Biome> getValue(int id) {
        return biomes.getKey(biomes.get(id));
    }

    @Override
    public String getName(int id) {
        return biomes.getName(id);
    }

    @Override
    public IntSet getRiverOverrides() {
        IntSet set = new IntOpenHashSet();
        for (Biome biome : biomes) {
            if (TFDefaultBiomes.overridesRiver(biome)) {
                set.add(biomes.getId(biome));
            }
        }
        return set;
    }

    @Override
    public Defaults<RegistryKey<Biome>> getDefaults() {
        return defaults;
    }

    @Override
    public Properties<RegistryKey<Biome>> getProperties() {
        return properties;
    }

    private static <T> TFBiomeContext decodeBiomeContext(Dynamic<T> dynamic) {
        return new TFBiomeContext(Codecs.decodeAndGet(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).codec(), dynamic));
    }

    private static <T> Dynamic<T> encodeBiomeContext(TFBiomeContext context, DynamicOps<T> ops) {
        return new Dynamic<>(ops, Codecs.encodeAndGet(RegistryLookupCodec.create(Registry.BIOME_REGISTRY).codec(), context.biomes.getRegistry(), ops));
    }

    public static TFBiomeContext dynamic() {
        return new TFBiomeContext(DynamicRegistries.builtin());
    }
}
