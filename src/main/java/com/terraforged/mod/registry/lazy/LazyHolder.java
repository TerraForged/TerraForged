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

package com.terraforged.mod.registry.lazy;

import com.mojang.datafixers.util.Either;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

public record LazyHolder<T>(T value, Supplier<ResourceKey<T>> key) implements Holder<T> {
    @Override
    public boolean isBound() {
        return true;
    }

    @Override
    public boolean is(ResourceLocation name) {
        return name.equals(key.get().location());
    }

    @Override
    public boolean is(ResourceKey<T> key) {
        return key == this.key.get();
    }

    @Override
    public boolean is(Predicate<ResourceKey<T>> test) {
        return test.test(key.get());
    }

    @Override
    public boolean is(TagKey<T> tag) {
        return false;
    }

    @Override
    public Stream<TagKey<T>> tags() {
        return Stream.empty();
    }

    @Override
    public Either<ResourceKey<T>, T> unwrap() {
        return Either.left(key.get());
    }

    @Override
    public Optional<ResourceKey<T>> unwrapKey() {
        return Optional.of(key.get());
    }

    @Override
    public Kind kind() {
        return Kind.DIRECT;
    }

    @Override
    public boolean isValidInRegistry(Registry<T> registry) {
        return registry.containsKey(key.get());
    }
}
