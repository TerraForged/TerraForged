///*
// *
// * MIT License
// *
// * Copyright (c) 2020 TerraForged
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
//package com.terraforged.mod.gui.element;
//
//import net.minecraft.client.gui.widget.AbstractButtonWidget;
//import net.minecraft.nbt.CompoundTag;
//import net.minecraftforge.fml.client.gui.widget.Slider;
//
//import java.util.List;
//
//// todo need to add forge slider lol
//public abstract class TerraSlider extends AbstractButtonWidget implements Slider.ISlider, Element {
//
//    private final CompoundTag value;
//    private final List<String> tooltip;
//
//    private Runnable callback = () -> {};
//
//    public TerraSlider(String prefix, CompoundTag value, boolean decimal) {
//        super(0, 0, 100, 20, prefix, "", value.getFloat("#min"), value.getFloat("#max"), 0F, decimal, true, b -> {});
//        this.value = value;
//        this.parent = this;
//        this.tooltip = Element.readTooltip(value);
//    }
//
//    public TerraSlider callback(Runnable callback) {
//        this.callback = callback;
//        return this;
//    }
//
//    @Override
//    public List<String> getTooltip() {
//        return tooltip;
//    }
//
//    @Override
//    public void onChangeSliderValue(Slider slider) {
//        onChange(slider, value);
//    }
//
//    @Override
//    public void onRelease(double mouseX, double mouseY) {
//        super.onRelease(mouseX, mouseY);
//        callback.run();
//    }
//
//    protected abstract void onChange(Slider slider, CompoundTag value);
//
//    public static class Int extends TerraSlider {
//
//        public Int(String prefix, CompoundTag value) {
//            super(prefix, value, false);
//            setValue(value.getInt("value"));
//            updateSlider();
//        }
//
//        @Override
//        protected void onChange(Slider slider, CompoundTag value) {
//            value.putInt("value", slider.getValueInt());
//        }
//    }
//
//    public static class Float extends TerraSlider {
//
//        public Float(String prefix, CompoundTag value) {
//            super(prefix, value, true);
//            precision = 3;
//            setValue(value.getFloat("value"));
//            updateSlider();
//        }
//
//        @Override
//        protected void onChange(Slider slider, CompoundTag value) {
//            int i = (int) (slider.getValue() * 1000);
//            float f = i / 1000F;
//            value.putFloat("value", f);
//        }
//    }
//}
