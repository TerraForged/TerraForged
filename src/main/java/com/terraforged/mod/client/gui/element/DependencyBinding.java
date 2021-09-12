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

package com.terraforged.mod.client.gui.element;

import com.terraforged.engine.serialization.serializer.Serializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraftforge.common.util.Constants;

import java.util.HashSet;
import java.util.Set;

public class DependencyBinding {

    public static final DependencyBinding NONE = new DependencyBinding();

    private final CompoundNBT value;
    private final String dependency;
    private final Set<String> allowedValues;

    private DependencyBinding() {
        value = null;
        dependency = null;
        allowedValues = null;
    }

    public DependencyBinding(CompoundNBT value, String dependency, ListNBT allowedValues) {
        this.value = value;
        this.dependency = dependency;
        this.allowedValues = new HashSet<>();
        for (net.minecraft.nbt.INBT option : allowedValues) {
            this.allowedValues.add(option.toString());
        }
    }

    public boolean isValid() {
        if (allowedValues == null) {
            return true;
        }

        Object dependencyValue = value.get(dependency);
        if (dependencyValue == null) {
            return false;
        }

        return allowedValues.contains(dependencyValue.toString());
    }

    public static DependencyBinding of(String name, CompoundNBT value) {
        String key = Serializer.META_PREFIX + name;
        CompoundNBT meta = value.getCompound(key);

        if (!meta.contains(Serializer.RESTRICTED)) {
            return DependencyBinding.NONE;
        }

        CompoundNBT restriction = meta.getCompound(Serializer.RESTRICTED);
        String dependency = restriction.getString(Serializer.RESTRICTED_NAME);
        ListNBT allowedValues = restriction.getList(Serializer.RESTRICTED_OPTIONS, Constants.NBT.TAG_STRING);
        if (allowedValues.size() == 0) {
            return DependencyBinding.NONE;
        }

        return new DependencyBinding(value, dependency, allowedValues);
    }
}
