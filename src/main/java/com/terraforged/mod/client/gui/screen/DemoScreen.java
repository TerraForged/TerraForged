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

package com.terraforged.mod.client.gui.screen;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.client.gui.screen.preview.PreviewPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class DemoScreen extends OverlayScreen {

    private static final int MESSAGE_COLOR = 0x00DDAA;
    public static final String LOGS = "";

    private final CreateWorldScreen parent;
    private final DimensionGeneratorSettings inputSettings;

    private final Instance instance;
    private final PreviewPage preview;
    private final String message = "TF-" + TerraForgedMod.getVersion() + " | Settings not available in this version!";

    private DimensionGeneratorSettings outputSettings;

    public DemoScreen(CreateWorldScreen parent, DimensionGeneratorSettings settings) {
        this.parent = parent;
        this.inputSettings = settings;
        this.outputSettings = settings;
        this.instance = new Instance(ConfigScreen.getInitialSettings(settings));
        this.preview = new PreviewPage(instance.settings, ConfigScreen.getSeed(parent), true);
    }

    @Override
    public void init() {
        preview.initPage(0, 30, this);

        int buttonsCenter = width / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonPad = 2;
        int buttonsRow = height - 25;

        // -52
        addButton(new Button(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.CANCEL.getText(), b -> closeScreen()));

        // +2
        addButton(new Button(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.DONE.getText(), b -> {
            Log.debug("Updating generator settings...");
            DynamicRegistries registries = parent.field_238934_c_.func_239055_b_();
            outputSettings = LevelType.updateOverworld(inputSettings, registries, instance.settings);
            Log.debug("Updating seed...");
            ConfigScreen.setSeed(parent, preview.getSeed());
            closeScreen();
        }));
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        preview.visit(pane -> pane.render(matrixStack, mouseX, mouseY, partialTicks));
        super.render(matrixStack, mouseX, mouseY, partialTicks);
        if (minecraft == null) {
            return;
        }
        minecraft.fontRenderer.drawString(matrixStack, message, 5, 10, MESSAGE_COLOR);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return preview.action(pane -> pane.mouseClicked(mouseX, mouseY, button)) || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        return preview.action(pane -> pane.mouseReleased(mouseX, mouseY, button)) || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        return preview.action(pane -> pane.mouseDragged(x, y, button, dx, dy)) || super.mouseDragged(x, y, button, dx, dy);
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        return preview.action(pane -> pane.mouseScrolled(x, y, direction)) || super.mouseScrolled(x, y, direction);
    }

    @Override
    public void closeScreen() {
        Log.debug("Returning to parent screen");
        preview.close();
        Minecraft.getInstance().displayGuiScreen(parent);
        parent.field_238934_c_.func_239043_a_(outputSettings);
    }
}
