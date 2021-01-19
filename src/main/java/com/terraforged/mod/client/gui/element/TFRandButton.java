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

package com.terraforged.mod.client.gui.element;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class TFRandButton extends TFButton {

    private final String name;
    private final String prefix;
    private final List<String> tooltip;
    private final CompoundNBT container;

    private int value;
    private final int min;
    private final int max;
    private Runnable callback = () -> {};

    public TFRandButton(String name, CompoundNBT value) {
        super(value.getString(name));
        this.name = name;
        this.container = value;
        this.value = value.getInt(name);
        this.prefix = Element.getDisplayName(name, value) + ": ";
        this.tooltip = Element.getToolTip(name, value);
        this.min = getLimit(value, name, "limit_lower", Integer.MIN_VALUE);
        this.max = getLimit(value, name, "limit_upper", Integer.MAX_VALUE);
        setMessage(new StringTextComponent(prefix + this.value));
    }

    public TFRandButton callback(Runnable runnable) {
        this.callback = runnable;
        return this;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public void onPress() {
        value = ThreadLocalRandom.current().nextInt(min, max);
        container.putInt(name, value);
        setMessage(new StringTextComponent(prefix + this.value));
        callback.run();
    }

    private static int getLimit(CompoundNBT value, String name, String limitType, int defaultLimit) {
        CompoundNBT meta = value.getCompound("#" + name);
        if (meta.contains(limitType)) {
            return meta.getInt(limitType);
        }
        return defaultLimit;
    }

    private static int clamp(int value, int min, int max) {
        if (value < min) {
            return min;
        }
        return Math.min(value, max);
    }
}
