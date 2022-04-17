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

package com.terraforged.mod.registry.hooks;

import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.ReflectionUtil;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.RegistryOps;

import java.lang.invoke.MethodHandle;
import java.util.Optional;

public class RegistryAccessUtil {
    private static final MethodHandle REGISTRY_ACCESS_GETTER = ReflectionUtil.field(RegistryOps.class, RegistryAccess.class);

    public static Optional<RegistryAccess> getRegistryAccess(DynamicOps<?> ops) {
        if (!(ops instanceof RegistryOps<?>)) {
            return Optional.empty();
        }

        try {
            return Optional.ofNullable(getRegistryAccess((RegistryOps<?>) ops));
        } catch (Throwable t) {
            t.printStackTrace();
            return Optional.empty();
        }
    }

    public static RegistryAccess getRegistryAccess(RegistryOps<?> ops) {
        try {
            return (RegistryAccess) REGISTRY_ACCESS_GETTER.invokeExact(ops);
        } catch (Throwable e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> MappedRegistry<T> copy(Registry<T> input) {
        var copy = new MappedRegistry<>(input.key(), input.lifecycle(), null);
        for (var e : input.entrySet()) {
            copy.register(e.getKey(), e.getValue(), input.lifecycle(e.getValue()));
        }
        return copy;
    }

    public static <T> void copy(Registry<T> registry, RegistryAccess.Writable holder) {
        var dest = (WritableRegistry<T>) holder.ownedRegistryOrThrow(registry.key());
        for (var entry : registry.entrySet()) {
            dest.register(entry.getKey(), entry.getValue(), registry.lifecycle(entry.getValue()));
        }
    }

    public static void printRegistryContents(Registry<?> registry) {
        if (!Environment.DEBUGGING) return;

        TerraForged.LOG.info(" - Registry: {}, Size: {}", registry.key().location(), registry.size());
        for (var entry : registry.entrySet()) {
            TerraForged.LOG.info("  - {}", entry.getKey().location());
        }
    }
}
