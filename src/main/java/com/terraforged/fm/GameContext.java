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

package com.terraforged.fm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.util.RegistryInstance;
import com.terraforged.fm.util.codec.Codecs;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryLookupCodec;
import net.minecraft.world.biome.Biome;

public class GameContext {

    public static final Codec<GameContext> CODEC = Codecs.create(GameContext::encodeGameContext, GameContext::decodeGameContext);

    public final RegistryInstance<Biome> biomes;

    public GameContext(DynamicRegistries.Impl registries) {
        this.biomes = new RegistryInstance<>(registries, Registry.BIOME_KEY);
    }

    public GameContext(Registry<Biome> biomes) {
        this.biomes = new RegistryInstance<>(biomes);
    }

    private static <T> GameContext decodeGameContext(Dynamic<T> dynamic) {
        return new GameContext(Codecs.decodeAndGet(RegistryLookupCodec.func_244331_a(Registry.BIOME_KEY).codec(), dynamic));
    }

    private static <T> Dynamic<T> encodeGameContext(GameContext context, DynamicOps<T> ops) {
        return new Dynamic<>(ops, Codecs.encodeAndGet(RegistryLookupCodec.func_244331_a(Registry.BIOME_KEY).codec(), context.biomes.getRegistry(), ops));
    }

    public static GameContext dynamic() {
        return new GameContext(DynamicRegistries.func_239770_b_());
    }
}
