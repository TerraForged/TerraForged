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

import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;

public abstract class TFSlider extends Slider implements Slider.ISlider, Element {

    protected final String name;
    private final CompoundNBT value;
    private final List<String> tooltip;

    private boolean lock = false;
    private Runnable callback = () -> {};

    public TFSlider(String name, CompoundNBT value, boolean decimal) {
        super(0, 0, 100, 20, new StringTextComponent(Element.getDisplayName(name, value) + ": "), new StringTextComponent(""), min(name, value), max(name, value), 0F, decimal, true, b -> {});
        this.name = name;
        this.value = value;
        this.parent = this;
        this.tooltip = Element.getToolTip(name, value);
    }

    public TFSlider callback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public void onChangeSliderValue(Slider slider) {
        if (!lock) {
            lock = true;
            onChange(slider, value);
            lock = false;
        }
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (dragging) {
            callback.run();
        }
        super.onRelease(mouseX, mouseY);
    }

    protected abstract void onChange(Slider slider, CompoundNBT value);

    private static float min(String name, CompoundNBT value) {
        CompoundNBT meta = value.getCompound("#" + name);
        return meta.getFloat("min");
    }

    private static float max(String name, CompoundNBT value) {
        CompoundNBT meta = value.getCompound("#" + name);
        return meta.getFloat("max");
    }

    public static class Int extends TFSlider {

        public Int(String name, CompoundNBT value) {
            super(name, value, false);
            setValue(value.getInt(name));
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            value.putInt(name, slider.getValueInt());
        }
    }

    public static class Float extends TFSlider {

        public Float(String name, CompoundNBT value) {
            super(name, value, true);
            precision = 3;
            setValue(value.getFloat(name));
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            int i = (int) (slider.getValue() * 1000);
            float f = i / 1000F;
            value.putFloat(name, f);
        }
    }

    // A slider who's min/max value are dynamically linked with some other slider/numeric value
    public abstract static class BoundSlider extends TFSlider {

        protected final float pad;
        protected final String lower;
        protected final String upper;

        public BoundSlider(String name, CompoundNBT value, float defaultPad, boolean decimal) {
            super(name, value, decimal);
            CompoundNBT meta = value.getCompound("#" + name);
            float pad = meta.getFloat("pad");
            this.pad = pad < 0 ? defaultPad : pad;
            this.lower = meta.getString("limit_lower");
            this.upper = meta.getString("limit_upper");
        }

        protected float getLower(CompoundNBT value) {
            if (lower == null || lower.isEmpty()) {
                return (float) (super.minValue - pad);
            }
            return value.getFloat(lower);
        }

        protected float getUpper(CompoundNBT value) {
            if (upper == null || upper.isEmpty()) {
                return (float) (super.maxValue + pad);
            }
            return value.getFloat(upper);
        }
    }

    // float (0.0-1.0) variant of the bound slider
    public static class BoundFloat extends BoundSlider {

        public BoundFloat(String name, CompoundNBT value) {
            this(name, value, 0.005F);
        }

        public BoundFloat(String name, CompoundNBT value, float pad) {
            super(name, value, pad, true);
            precision = 3;
            setValue(value.getFloat(name));
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            int i = (int) (slider.getValue() * 1000);

            float lower = getLower(value) + pad;
            float upper = getUpper(value) - pad;
            float val = NoiseUtil.clamp(i / 1000F, lower, upper);

            // update setting value
            value.putFloat(name, val);

            // update actual slider value
            setValue(val);
            updateSlider();
        }
    }

    // int variant of the bound slider
    public static class BoundInt extends BoundSlider {

        public BoundInt(String name, CompoundNBT value) {
            this(name, value, 1);
        }

        public BoundInt(String name, CompoundNBT value, int pad) {
            super(name, value, pad, false);
            setValue(value.getInt(name));
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            int i = slider.getValueInt();

            float lower = getLower(value) + pad;
            float upper = getUpper(value) - pad;
            int val = NoiseUtil.round(NoiseUtil.clamp(i, lower, upper));

            // update setting value
            value.putInt(name, val);

            // update actual slider value
            setValue(val);
            updateSlider();
        }
    }
}
