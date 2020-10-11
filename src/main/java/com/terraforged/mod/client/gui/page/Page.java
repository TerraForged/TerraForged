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

package com.terraforged.mod.client.gui.page;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.client.gui.config.OverlayRenderer;
import com.terraforged.mod.client.gui.config.OverlayScreen;
import com.terraforged.mod.client.gui.config.ScrollPane;
import com.terraforged.mod.client.gui.element.Element;
import com.terraforged.mod.client.gui.element.TerraLabel;
import com.terraforged.mod.client.gui.element.TerraSlider;
import com.terraforged.mod.client.gui.element.TerraTextInput;
import com.terraforged.mod.client.gui.element.TerraToggle;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraftforge.common.util.Constants;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class Page implements IGuiEventListener, OverlayRenderer {

    protected static final Runnable NO_CALLBACK = () -> {};

    private static final int SLIDER_HEIGHT = 20;
    private static final int SLIDER_PAD = 2;

    private final Column[] columns;
    private final float[] sizes;
    private final int hpad;
    private final int vpad;
    protected OverlayScreen parent;

    public Page(int hpad, int vpad, float... columnSizes) {
        this.hpad = hpad;
        this.vpad = vpad;
        this.sizes = columnSizes;
        this.columns = new Column[columnSizes.length];
    }

    public abstract void callback(Runnable runnable);

    public abstract void save();

    public abstract void init(OverlayScreen parent);

    @Override
    public void renderOverlays(MatrixStack matrixStack, Screen screen, int mouseX, int mouseY) {
        for (Column column : columns) {
            if (column.scrollPane.getEventListeners().isEmpty()) {
                continue;
            }
            column.scrollPane.renderOverlays(matrixStack, screen, mouseX, mouseY);
        }
    }

    public void visit(Consumer<ScrollPane> consumer) {
        for (Column column : columns) {
            if (column.scrollPane.getEventListeners().isEmpty()) {
                continue;
            }
            consumer.accept(column.scrollPane);
        }
    }

    public boolean action(Function<ScrollPane, Boolean> action) {
        boolean result = false;
        for (Column column : columns) {
            if (column.scrollPane.getEventListeners().isEmpty()) {
                continue;
            }
            boolean b = action.apply(column.scrollPane);
            result = b || result;
        }
        return result;
    }

    public void close() {

    }

    public String getTitle() {
        return "";
    }

    public Column getColumn(int index) {
        return columns[index];
    }

    public final void initPage(int marginH, int marginV, OverlayScreen parent) {
        this.parent = parent;
        int top = marginV;
        int left = marginH;
        int pageWidth = parent.width - (marginH * 2);
        int pageHeight = parent.height;
        for (int i = 0; i < columns.length; i++) {
            int columnWidth = Math.round(sizes[i] * pageWidth) - (2 * hpad);
            Column column = new Column(left, top, columnWidth, pageHeight, hpad, vpad);
            columns[i] = column;
            left += columnWidth + (2 * hpad);
        }
        init(parent);
    }

    public void addElements(int x, int y, Column column, CompoundNBT settings, Consumer<Widget> consumer, Runnable callback) {
        addElements(x, y, column, settings, false, consumer, callback);
    }

    public void addElements(int x, int y, Column column, CompoundNBT settings, boolean deep, Consumer<Widget> consumer, Runnable callback) {
        AtomicInteger top = new AtomicInteger(y);

        NBTHelper.streamkeys(settings).forEach(name -> {
            Widget button = createButton(name, settings, callback);
            if (button != null) {
                button.setWidth(column.width);
                button.setHeight(SLIDER_HEIGHT);
                button.x = x;
                button.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                consumer.accept(button);
                onAddWidget(button);
            } else if (deep) {
                INBT child = settings.get(name);
                if (child == null || child.getId() != Constants.NBT.TAG_COMPOUND) {
                    return;
                }
                TerraLabel label = new TerraLabel(Element.getDisplayName(name, settings));
                label.x = x;
                label.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                consumer.accept(label);
                addElements(x, label.y, column, (CompoundNBT) child, consumer, callback);
            }
        });
    }

    public Widget createButton(String name, CompoundNBT value, Runnable callback) {
        INBT tag = value.get(name);
        if (tag == null) {
            return null;
        }

        byte type = tag.getId();
        if (type == Constants.NBT.TAG_INT) {
            if (hasLimit(name, value)) {
                return new TerraSlider.BoundInt(name, value).callback(callback);
            }
            return new TerraSlider.Int(name, value).callback(callback);
        } else if (type == Constants.NBT.TAG_FLOAT) {
            if (hasLimit(name, value)) {
                return new TerraSlider.BoundFloat(name, value).callback(callback);
            }
            return new TerraSlider.Float(name, value).callback(callback);
        } else if (type == Constants.NBT.TAG_STRING && hasOptions(name, value)) {
            return new TerraToggle(name, value).callback(callback);
        } else if (type == Constants.NBT.TAG_STRING) {
            return new TerraTextInput(name, value);
        } else {
            return null;
        }
    }

    public void onAddWidget(Widget widget) {

    }

    public static class Column {

        public final int left;
        public final int right;
        public final int top;
        public final int bottom;
        public final int width;
        public final int height;
        public final ScrollPane scrollPane;

        private Column(int left, int top, int width, int height, int vpad, int hpad) {
            this.left = left + vpad;
            this.right = left + width - vpad;
            this.top = top + hpad;
            this.bottom = height - hpad;
            this.width = width;
            this.height = height;
            this.scrollPane = new ScrollPane(25);
            this.scrollPane.updateSize(width, height, 30, height - 30);
            this.scrollPane.setLeftPos(this.left);
        }
    }

    private static boolean hasOptions(String name, CompoundNBT value) {
        return value.getCompound("#" + name).contains("options");
    }

    private static boolean hasLimit(String name, CompoundNBT value) {
        return value.getCompound("#" + name).contains("limit_lower") || value.getCompound("#" + name).contains("limit_upper");
    }
}
