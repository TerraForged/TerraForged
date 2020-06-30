/*
 *
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

package com.terraforged.mod.util.nbt;

import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.JsonOps;
import com.terraforged.core.serialization.serializer.Serializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTDynamicOps;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class NBTHelper {

    public static JsonElement toJson(CompoundNBT tag) {
        Dynamic<INBT> input = new Dynamic<>(NBTDynamicOps.INSTANCE, tag);
        Dynamic<JsonElement> output = input.convert(JsonOps.INSTANCE);
        return output.getValue();
    }

    public static CompoundNBT fromJson(JsonElement json) {
        Dynamic<JsonElement> input = new Dynamic<>(JsonOps.INSTANCE, json);
        Dynamic<INBT> output = input.convert(NBTDynamicOps.INSTANCE);
        return (CompoundNBT) output.getValue();
    }

    public static CompoundNBT serialize(Object object) {
        return serialize("", object);
    }

    public static CompoundNBT serialize(String owner, Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            Serializer.serialize(object, writer, owner, true);
            return writer.compound();
        } catch (IllegalAccessException e) {
            return new CompoundNBT();
        }
    }

    public static CompoundNBT serializeCompact(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            return stripMetadata(writer.compound());
        } catch (IllegalAccessException e) {
            return new CompoundNBT();
        }
    }

    public static CompoundNBT readCompact(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            new Serializer().serialize(object, writer);
            return writer.compound();
        } catch (IllegalAccessException e) {
            return new CompoundNBT();
        }
    }

    public static Stream<String> streamkeys(CompoundNBT compound) {
        return compound.keySet().stream()
                .filter(name -> !name.startsWith("#"))
                .sorted(Comparator.comparing(name -> compound.getCompound("#" + name).getInt("order")));
    }

    public static <T extends INBT> T stripMetadata(T tag) {
        if (tag instanceof CompoundNBT) {
            CompoundNBT compound = (CompoundNBT) tag;
            List<String> keys = new LinkedList<>(compound.keySet());
            for (String key : keys) {
                if (key.charAt(0) == '#') {
                    compound.remove(key);
                } else {
                    stripMetadata(compound.get(key));
                }
            }
        } else if (tag instanceof ListNBT) {
            ListNBT list = (ListNBT) tag;
            for (int i = 0; i < list.size(); i++) {
                stripMetadata(list.get(i));
            }
        }
        return tag;
    }

    public static boolean deserialize(CompoundNBT settings, Object object) {
        try {
            NBTReader reader = new NBTReader(settings);
            return reader.writeTo(object);
        } catch (Throwable e) {
            e.printStackTrace();
            return false;
        }
    }
}
