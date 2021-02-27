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
import net.minecraft.client.resources.I18n;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextComponent;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public interface Element {

    AtomicInteger ID_COUNTER = new AtomicInteger(0);

    default ITextComponent getToolTipText() {
        TextComponent base = new StringTextComponent("");
        for (String s : getTooltip()) {
            base.append(new StringTextComponent(s));
        }
        return base;
    }

    default List<String> getTooltip() {
        return Collections.emptyList();
    }

    static int nextID() {
        return ID_COUNTER.getAndAdd(1);
    }

    static String getDisplayName(String name, CompoundNBT value) {
        if (name.contains(":")) {
            return name;
        }
        String key = getDisplayKey(name, value);
        if (key.endsWith(".")) {
            return "";
        }
        return I18n.format(key);
    }

    static List<String> getToolTip(String name, CompoundNBT value) {
        String key = getCommentKey(name, value);
        if (key.endsWith(".")) {
            return Collections.emptyList();
        }
        return Collections.singletonList(I18n.format(key));
    }

    static String getDisplayKey(String name, CompoundNBT value) {
        return "display.terraforged." + getKey(name, value);
    }

    static String getCommentKey(String name, CompoundNBT value) {
        return "tooltip.terraforged." + getKey(name, value);
    }

    static String getKey(String name, CompoundNBT value) {
        return value.getCompound(Serializer.META_PREFIX + name).getString(Serializer.KEY);
    }
}
