///*
// * MIT License
// *
// * Copyright (c) 2021 TerraForged
// *
// * Permission is hereby granted, free of charge, to any person obtaining a copy
// * of this software and associated documentation files (the "Software"), to deal
// * in the Software without restriction, including without limitation the rights
// * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
// * copies of the Software, and to permit persons to whom the Software is
// * furnished to do so, subject to the following conditions:
// *
// * The above copyright notice and this permission notice shall be included in all
// * copies or substantial portions of the Software.
// *
// * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
// * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
// * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
// * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
// * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
// * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
// * SOFTWARE.
// */
//
//package com.terraforged.mod.registry.hooks;
//
//import com.google.common.collect.ImmutableMap;
//import com.mojang.serialization.Codec;
//import com.mojang.serialization.codecs.UnboundedMapCodec;
//import com.terraforged.mod.TerraForged;
//import net.minecraft.core.Registry;
//import net.minecraft.core.RegistryAccess;
//
//import java.util.Map;
//
//public class NetworkCodecHook {
//    @SuppressWarnings({"unchecked"})
//    public static <K, V> Codec<RegistryAccess> createCodec(UnboundedMapCodec<K, V> codec) {
//        TerraForged.LOG.info("Injecting safe world-gen network codec");
//
//        return codec.xmap(NetworkCodecHook::createHolder, holder -> {
//            var builder = ImmutableMap.<K, V>builder();
//            for (var known  : RegistryAccess.knownRegistries()) {
//                if (known.sendToClient()) {
//                    builder.put((K) known.key(), (V) holder.ownedRegistryOrThrow(known.key()));
//                }
//            }
//            return builder.build();
//        });
//    }
//
//    private static <K, V> RegistryAccess.RegistryHolder createHolder(Map<K, V> map) {
//        var holder = new RegistryAccess.RegistryHolder();
//
//        for (var entry : map.entrySet()) {
//            var registry = (Registry<?>) entry.getValue();
//            RegistryAccessUtil.copy(registry, holder);
//        }
//
//        return holder;
//    }
//}
