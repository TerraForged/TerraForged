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

package com.terraforged.mod.client.gui;

import com.terraforged.mod.client.gui.element.CheckBox;
import com.terraforged.mod.client.gui.element.Element;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

public class OverlayScreen extends Screen implements OverlayRenderer {

    public boolean showTooltips = false;

    public OverlayScreen() {
        super(new TranslationTextComponent(""));
        super.minecraft = Minecraft.getInstance();
        super.font = minecraft.fontRenderer;
    }

    @Override
    public <T extends Widget> T addButton(T buttonIn) {
        return super.addButton(buttonIn);
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.render(mouseX, mouseY, partialTicks);
        if (showTooltips) {
            renderOverlays(this, mouseX, mouseY);
        }
    }

    @Override
    public void renderOverlays(Screen screen, int mouseX, int mouseY) {
        for (Widget button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                if (button instanceof Element) {
                    screen.renderTooltip(((Element) button).getTooltip(), mouseX, mouseY);
                    return;
                }
            }
        }
    }

    @Override
    protected void init() {
        addButton(new CheckBox("Tooltips", showTooltips) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                showTooltips = isChecked();
            }

            @Override
            public void render(int mouseX, int mouseY, float partial) {
                this.x = OverlayScreen.this.width - width - 13;
                this.y = 6;
                super.render(mouseX, mouseY, partial);
            }
        });
    }
}
