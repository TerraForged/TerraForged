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

package com.terraforged.mod.featuremanager.util.codec;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.OptionalDynamic;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.gen.treedecorator.TreeDecorator;
import net.minecraft.world.gen.treedecorator.TreeDecoratorType;

public class CodecHelper {

    public static <T> BlockState getState(Dynamic<T> dynamic) {
        return Codecs.getResult(BlockState.CODEC.decode(dynamic))
                .map(Pair::getFirst)
                .orElseGet(Blocks.AIR::defaultBlockState);
    }

    public static <T> BlockState getState(OptionalDynamic<T> dynamic) {
        return dynamic.result().map(CodecHelper::getState)
                .orElseGet(Blocks.AIR::defaultBlockState);
    }

    public static <T> T setState(BlockState state, DynamicOps<T> ops) {
        return Codecs.getResult(BlockState.CODEC.encodeStart(ops, state))
                .orElseThrow(CodecException.get("Cannot encode BlockState %s", state));
    }

    public static <T> TreeDecorator treeDecorator(TreeDecoratorType<?> type, T t, DynamicOps<T> ops) {
        return Codecs.getResult(type.codec().decode(ops, t).map(Pair::getFirst))
                .orElseThrow(CodecException.get("Cannot decode TreeDecoratorType(%s) %s", type, t));
    }
}
