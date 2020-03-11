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

package com.terraforged.mod.gui.page;

import com.terraforged.mod.gui.OverlayRenderer;
import com.terraforged.mod.gui.OverlayScreen;
import com.terraforged.mod.gui.ScrollPane;
import com.terraforged.mod.gui.element.TerraButton;
import com.terraforged.mod.gui.element.TerraLabel;
import com.terraforged.mod.gui.element.TerraSlider;
import com.terraforged.mod.gui.element.Toggle;
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

    public abstract void save();

    public abstract void init(OverlayScreen parent);

    @Override
    public void renderOverlays(Screen screen, int mouseX, int mouseY) {
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
                continue;
            }
            column.scrollPane.renderOverlays(screen, mouseX, mouseY);
        }
    }

    public void visit(Consumer<ScrollPane> consumer) {
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
                continue;
            }
            consumer.accept(column.scrollPane);
        }
    }

    public boolean action(Function<ScrollPane, Boolean> action) {
        boolean result = false;
        for (Column column : columns) {
            if (column.scrollPane.children().isEmpty()) {
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

        NBTHelper.stream(settings).forEach(value -> {
            String name = value.getString("#display");
            Widget button = createButton(name, value, callback);
            if (button != null) {
                button.setWidth(column.width);
                button.setHeight(SLIDER_HEIGHT);
                button.x = x;
                button.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                consumer.accept(button);
            } else if (deep) {
                INBT child = value.get("value");
                if (child == null || child.getId() != Constants.NBT.TAG_COMPOUND) {
                    return;
                }
                TerraLabel label = new TerraLabel(name);
                label.x = x;
                label.y = top.getAndAdd(SLIDER_HEIGHT + SLIDER_PAD);
                consumer.accept(label);
                addElements(x, label.y, column, (CompoundNBT) child, consumer, callback);
            }
        });
    }

    public Widget createButton(String name, CompoundNBT value, Runnable callback) {
        INBT tag = value.get("value");
        if (tag == null) {
            return null;
        }

        byte type = tag.getId();
        if (type == Constants.NBT.TAG_INT) {
            return new TerraSlider.Int(name + ": ", value).callback(callback);
        } else if (type == Constants.NBT.TAG_FLOAT) {
            return new TerraSlider.Float(name + ": ", value).callback(callback);
        } else if (type == Constants.NBT.TAG_STRING && value.contains("#options")) {
            return new Toggle(name + ": ", value).callback(callback);
        } else if (type == Constants.NBT.TAG_STRING) {
            return new TerraButton(name);
        } else {
            return null;
        }
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
            this.scrollPane = new ScrollPane(22);
            this.scrollPane.updateSize(width, height, 30, height - 30);
            this.scrollPane.setLeftPos(this.left);
        }
    }
}
