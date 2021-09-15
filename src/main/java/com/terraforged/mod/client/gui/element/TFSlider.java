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

import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.engine.serialization.serializer.Serializer;
import com.terraforged.noise.util.NoiseUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.fml.client.gui.widget.Slider;

import java.util.List;

public abstract class TFSlider extends Slider implements Slider.ISlider, Element {

    protected final String name;
    private final CompoundNBT value;
    private final List<String> tooltip;
    private final DependencyBinding binding;

    private boolean lock = false;
    private Runnable callback = () -> {};

    public TFSlider(String name, CompoundNBT value, boolean decimal) {
        super(0, 0, 100, 20, new StringTextComponent(Element.getDisplayName(name, value) + ": "), new StringTextComponent(""), min(name, value), max(name, value), 0F, decimal, true, b -> {});
        this.name = name;
        this.value = value;
        this.parent = this;
        this.tooltip = Element.getToolTip(name, value);
        this.binding = DependencyBinding.of(name, value);
    }

    public TFSlider callback(Runnable callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void render(MatrixStack stack, int x, int y, float delta) {
        super.active = binding.isValid();
        super.render(stack, x, y, delta);
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (button == 0) {
            return super.mouseClicked(x, y, button);
        }

        if (clicked(x, y)) {
            reset();
            callback.run();
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }

        return false;
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

    protected abstract void reset();

    private static float min(String name, CompoundNBT value) {
        CompoundNBT meta = value.getCompound(Serializer.META_PREFIX + name);
        return meta.getFloat(Serializer.BOUND_MIN);
    }

    private static float max(String name, CompoundNBT value) {
        CompoundNBT meta = value.getCompound(Serializer.META_PREFIX + name);
        return meta.getFloat(Serializer.BOUND_MAX);
    }

    public static class Int extends TFSlider {

        protected final int defaultValue;

        public Int(String name, CompoundNBT value) {
            super(name, value, false);
            defaultValue = value.getInt(name);
            setValue(defaultValue);
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            value.putInt(name, slider.getValueInt());
        }

        @Override
        protected void reset() {
            setValue(defaultValue);
            updateSlider();
        }
    }

    public static class Float extends TFSlider {

        protected final float defaultValue;

        public Float(String name, CompoundNBT value) {
            super(name, value, true);
            precision = 3;
            defaultValue = value.getFloat(name);
            setValue(defaultValue);
            updateSlider();
        }

        @Override
        protected void onChange(Slider slider, CompoundNBT value) {
            int i = (int) (slider.getValue() * 1000);
            float f = i / 1000F;
            value.putFloat(name, f);
        }

        @Override
        protected void reset() {
            setValue(defaultValue);
            updateSlider();
        }
    }

    // A slider who's min/max value are dynamically linked with some other slider/numeric value
    public abstract static class BoundSlider extends TFSlider {

        protected final float pad;
        protected final String lower;
        protected final String upper;

        public BoundSlider(String name, CompoundNBT value, float defaultPad, boolean decimal) {
            super(name, value, decimal);
            CompoundNBT meta = value.getCompound(Serializer.META_PREFIX + name);
            float pad = meta.getFloat(Serializer.LINK_PAD);
            this.pad = pad < 0 ? defaultPad : pad;
            this.lower = meta.getString(Serializer.LINK_LOWER);
            this.upper = meta.getString(Serializer.LINK_UPPER);
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

        private final float defaultValue;

        public BoundFloat(String name, CompoundNBT value) {
            this(name, value, 0.005F);
        }

        public BoundFloat(String name, CompoundNBT value, float pad) {
            super(name, value, pad, true);
            precision = 3;
            defaultValue = value.getFloat(name);
            setValue(defaultValue);
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

        @Override
        protected void reset() {
            setValue(defaultValue);
            updateSlider();
        }
    }

    // int variant of the bound slider
    public static class BoundInt extends BoundSlider {

        private final int defaultValue;

        public BoundInt(String name, CompoundNBT value) {
            this(name, value, 1);
        }

        public BoundInt(String name, CompoundNBT value, int pad) {
            super(name, value, pad, false);
            this.defaultValue = value.getInt(name);
            setValue(defaultValue);
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

        @Override
        protected void reset() {
            setValue(defaultValue);
            updateSlider();
        }
    }
}
