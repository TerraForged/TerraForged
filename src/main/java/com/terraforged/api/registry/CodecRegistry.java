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

package com.terraforged.api.registry;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.util.codec.Codecs;
import com.terraforged.fm.util.codec.CodecException;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.IForgeRegistryEntry;
import net.minecraftforge.registries.RegistryBuilder;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;

public class CodecRegistry<E extends IForgeRegistryEntry<E>> implements IForgeRegistry<E> {

    private final Codec<E> codec;
    private final IForgeRegistry<E> delegate;

    public CodecRegistry(IForgeRegistry<E> delegate) {
        this.delegate = delegate;
        this.codec = Codecs.create(this::encode, this::decode);
    }

    public Codec<E> getCodec() {
        return codec;
    }

    @Override
    public ResourceLocation getRegistryName() {
        return delegate.getRegistryName();
    }

    @Override
    public Class<E> getRegistrySuperType() {
        return delegate.getRegistrySuperType();
    }

    @Override
    public void register(E value) {
        delegate.register(value);
    }

    @Override
    public void registerAll(E... values) {
        delegate.registerAll(values);
    }

    @Override
    public boolean containsKey(ResourceLocation key) {
        return delegate.containsKey(key);
    }

    @Override
    public boolean containsValue(E value) {
        return delegate.containsValue(value);
    }

    @Override
    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    @Override
    @Nullable
    public E getValue(ResourceLocation key) {
        return delegate.getValue(key);
    }

    @Override
    @Nullable
    public ResourceLocation getKey(E value) {
        return delegate.getKey(value);
    }

    @Override
    @Nullable
    public ResourceLocation getDefaultKey() {
        return delegate.getDefaultKey();
    }

    @Override
    @Nonnull
    public Set<ResourceLocation> getKeys() {
        return delegate.getKeys();
    }

    @Override
    @Nonnull
    public Collection<E> getValues() {
        return delegate.getValues();
    }

    @Override
    @Nonnull
    public Set<Map.Entry<RegistryKey<E>, E>> getEntries() {
        return delegate.getEntries();
    }

    @Override
    public <T1> T1 getSlaveMap(ResourceLocation slaveMapName, Class<T1> type) {
        return delegate.getSlaveMap(slaveMapName, type);
    }

    @Override
    public Iterator<E> iterator() {
        return delegate.iterator();
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        delegate.forEach(action);
    }

    @Override
    public Spliterator<E> spliterator() {
        return delegate.spliterator();
    }

    private <T> E decode(Dynamic<T> dynamic) {
        String value = dynamic.getOps().getStringValue(dynamic.getValue()).result().orElseThrow(CodecException.get("Missing registry key"));
        try {
            ResourceLocation name = new ResourceLocation(value);
            E e = delegate.getValue(name);
            if (e == null) {
                throw CodecException.of("Missing registry entry for %s", name);
            }
            return e;
        } catch (Throwable t) {
            throw CodecException.of(t, "Exception parsing %s", value);
        }
    }

    private <T> Dynamic<T> encode(E e, DynamicOps<T> ops) {
        ResourceLocation name = delegate.getKey(e);
        if (name == null) {
            throw CodecException.of("Missing registry key for %s", e);
        }
        return new Dynamic<>(ops, ops.createString(name.toString()));
    }

    public static <E extends IForgeRegistryEntry<E>> CodecRegistry<E> of(RegistryBuilder<E> builder) {
        return new CodecRegistry<>(builder.create());
    }
}
