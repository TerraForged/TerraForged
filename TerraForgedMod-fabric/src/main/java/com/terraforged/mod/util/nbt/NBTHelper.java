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
import com.terraforged.core.util.serialization.serializer.Serializer;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class NBTHelper {

    public static JsonElement toJson(CompoundTag tag) {
        Dynamic<Tag> input = new Dynamic<>(NbtOps.INSTANCE, tag);
        Dynamic<JsonElement> output = input.convert(JsonOps.INSTANCE);
        return output.getValue();
    }

    public static Stream<CompoundTag> stream(CompoundTag tag) {
        return tag.getKeys().stream()
                .map(tag::getCompound)
                .sorted(Comparator.comparing(t -> t.getInt("#order")));

    }

    public static CompoundTag serialize(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            return writer.compound();
        } catch (IllegalAccessException e) {
            return new CompoundTag();
        }
    }

    public static CompoundTag serializeCompact(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            return stripMetadata(writer.compound());
        } catch (IllegalAccessException e) {
            return new CompoundTag();
        }
    }

    public static CompoundTag readCompact(Object object) {
        try {
            NBTWriter writer = new NBTWriter();
            writer.readFrom(object);
            new Serializer().serialize(object, writer);
            return writer.compound();
        } catch (IllegalAccessException e) {
            return new CompoundTag();
        }
    }

    public static <T extends Tag> T stripMetadata(T tag) {
        if (tag instanceof CompoundTag) {
            CompoundTag compound = (CompoundTag) tag;
            List<String> keys = new LinkedList<>(compound.getKeys());
            for (String key : keys) {
                if (key.charAt(0) == '#') {
                    compound.remove(key);
                } else {
                    stripMetadata(compound.get(key));
                }
            }
        } else if (tag instanceof ListTag) {
            ListTag list = (ListTag) tag;
            for (int i = 0; i < list.size(); i++) {
                stripMetadata(list.get(i));
            }
        }
        return tag;
    }

    public static void deserialize(CompoundTag settings, Object object) {
        try {
            NBTReader reader = new NBTReader(settings);
            reader.writeTo(object);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }
}
