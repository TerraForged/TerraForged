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

package com.terraforged.mod.gui;

import com.terraforged.mod.gui.element.Element;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.AbstractButtonWidget;
import net.minecraft.client.gui.widget.ElementListWidget;

import java.util.Collections;
import java.util.List;

public class ScrollPane extends ElementListWidget<ScrollPane.Entry> implements OverlayRenderer {

    private boolean hovered = false;

    public ScrollPane(int slotHeightIn) {
        super(MinecraftClient.getInstance(), 0, 0, 0, 0, slotHeightIn);
    }

    public void addButton(AbstractButtonWidget button) {
        super.addEntry(new Entry(button));
    }

    @Override
    public void renderOverlays(Screen screen, int x, int y) {
        for (Entry entry : this.children()) {
            if (/*entry.isMouseOver(x, y) && todo lost*/ entry.option.isMouseOver(x, y)) {
                AbstractButtonWidget button = entry.option;
                if (button instanceof Element) {
                    screen.renderTooltip(((Element) button).getTooltip(), x, y);
                    return;
                }
            }
        }
    }

    @Override
    public int getRowWidth() {
        return width - 20;
    }

    @Override
    public void render(int x, int y, float partialTicks) {
        super.render(x, y, partialTicks);
        hovered = isMouseOver(x, y);
    }

    @Override
    protected int getScrollbarPosition() {
        return getMaxPosition(); // todo old getRight
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        return hovered && super.mouseScrolled(x, y, direction);
    }

    public class Entry extends ElementListWidget.Entry<Entry> {

        public final AbstractButtonWidget option;

        public Entry(AbstractButtonWidget option) {
            this.option = option;
        }

        @Override
        public List<? extends net.minecraft.client.gui.Element> children() {
            return Collections.singletonList(option);
        }

        @Override
        public boolean mouseClicked(double x, double y, int button) {
            return option.mouseClicked(x, y, button);
        }

        @Override
        public boolean mouseReleased(double x, double y, int button) {
            return option.mouseReleased(x, y, button);
        }

        @Override
        public void render(int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean wut, float partialTicks) {
            int optionWidth = Math.min(396, width);
            int padding = (width - optionWidth) / 2;
            option.x = left + padding;
            option.y = top;
            option.visible = true;
            option.setWidth(optionWidth);
            // option.height = height; todo maybe use accessor
            //if (option instanceof Preview) {
            //    option.setHeight(option.getWidth());
            //}
            option.render(mouseX, mouseY, partialTicks);
        }
    }
}
