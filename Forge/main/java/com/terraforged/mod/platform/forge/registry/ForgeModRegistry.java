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

package com.terraforged.mod.platform.forge.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.registry.key.RegistryKey;
import net.minecraft.core.MappedRegistry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.RegistryBuilder;

public class ForgeModRegistry<T> extends ForgeRegistry<T> implements ModRegistry<T> {
    protected final Codec<T> codec;

    public ForgeModRegistry(RegistryKey<T> key, Codec<T> codec) {
        super(key);
        this.codec = codec;
        this.register.makeRegistry(() -> new RegistryBuilder<T>().disableSaving().dataPackRegistry(this.codec));
    }

    @Override
    public Codec<T> codec() {
        return codec;
    }

    @Override
    public MappedRegistry<T> copy() {
        var copy = new MappedRegistry<T>(key.get(), Lifecycle.stable(), null);
        for (var entry : register.getEntries()) {
            var key = ResourceKey.create(this.key.get(), entry.getId());
            copy.register(key, entry.get(), Lifecycle.stable());
        }
        return copy;
    }
}
