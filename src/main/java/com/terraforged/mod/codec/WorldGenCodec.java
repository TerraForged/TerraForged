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

package com.terraforged.mod.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.registry.RegistryAccessUtil;
import net.minecraft.core.RegistryAccess;

public interface WorldGenCodec<V> extends Codec<V> {
    <T> V decode(DynamicOps<T> ops, T input, RegistryAccess access);

    <T> T encode(V v, DynamicOps<T> ops);

    @Override
    default <T> DataResult<Pair<V, T>> decode(DynamicOps<T> ops, T input) {
        var access = RegistryAccessUtil.getRegistryAccess(ops);
        if (access.isEmpty()) return DataResult.error("Invalid ops");

        var result = decode(ops, input, access.get());
        return DataResult.success(Pair.of(result, input));
    }

    @Override
    default <T> DataResult<T> encode(V input, DynamicOps<T> ops, T prefix) {
        return DataResult.success(encode(input, ops));
    }
}
