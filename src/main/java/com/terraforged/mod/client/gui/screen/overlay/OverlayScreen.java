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

package com.terraforged.mod.client.gui.screen.overlay;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.element.Element;
import com.terraforged.mod.client.gui.element.TFCheckBox;
import com.terraforged.mod.client.gui.screen.preview.PreviewSettings;
import com.terraforged.mod.config.ConfigManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.util.text.TranslationTextComponent;

public class OverlayScreen extends Screen implements OverlayRenderer {

    private final CommentedFileConfig config;

    public OverlayScreen() {
        super(new TranslationTextComponent(""));
        super.minecraft = Minecraft.getInstance();
        super.font = minecraft.fontRenderer;
        this.config = ConfigManager.GENERAL.get();
        PreviewSettings.showTooltips = config.getOrElse(GuiKeys.TOOLTIPS_KEY, true);
        PreviewSettings.showCoords = config.getOrElse(GuiKeys.COORDS_KEY, false);
    }

    @Override
    public <T extends Widget> T addButton(T buttonIn) {
        return super.addButton(buttonIn);
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (PreviewSettings.showTooltips) {
            renderOverlays(matrixStack, this, mouseX, mouseY);
        }
    }

    @Override
    public void renderOverlays(MatrixStack matrixStack, Screen screen, int mouseX, int mouseY) {
        for (Widget button : buttons) {
            if (button.isMouseOver(mouseX, mouseY)) {
                if (button instanceof Element) {
                    Element element = (Element) button;
                    if (!element.getTooltip().isEmpty()) {
                        screen.renderTooltip(matrixStack, element.getToolTipText(), mouseX, mouseY);
                    }
                    return;
                }
            }
        }
    }

    @Override
    protected void init() {
        addButton(new TFCheckBox(GuiKeys.TOOLTIPS.get(), PreviewSettings.showTooltips) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                PreviewSettings.showTooltips = isChecked();
                config.set(GuiKeys.TOOLTIPS_KEY, PreviewSettings.showTooltips);
                config.save();
            }

            @Override
            public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
                this.x = OverlayScreen.this.width - width - 13;
                this.y = 6;
                super.render(matrixStack, mouseX, mouseY, partial);
            }
        });

        addButton(new TFCheckBox(GuiKeys.COORDS.get(), PreviewSettings.showCoords) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                PreviewSettings.showCoords = isChecked();
                config.set(GuiKeys.COORDS_KEY, PreviewSettings.showCoords);
                config.save();
            }

            @Override
            public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partial) {
                setChecked(PreviewSettings.showCoords);
                this.x = OverlayScreen.this.width - (width * 2) - 15;
                this.y = 6;
                super.render(matrixStack, mouseX, mouseY, partial);
            }
        });
    }
}
