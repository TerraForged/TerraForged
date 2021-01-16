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

package com.terraforged.mod.client.gui.screen;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.chunk.TerraContext;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.element.TFLabel;
import com.terraforged.mod.client.gui.page.Page;
import com.terraforged.mod.client.gui.page.SimplePage;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.client.gui.screen.page.PresetsPage;
import com.terraforged.mod.client.gui.screen.page.SimplePreviewPage;
import com.terraforged.mod.client.gui.screen.page.WorldPage;
import com.terraforged.mod.client.gui.screen.preview.PreviewPage;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.gen.settings.DimensionGeneratorSettings;

public class ConfigScreen extends OverlayScreen {

    private static final Button.IPressable NO_ACTION = b -> {};

    private final Page[] pages;
    private final PreviewPage preview;
    private final CreateWorldScreen parent;
    private final DimensionGeneratorSettings inputSettings;
    private final Instance instance;

    private int pageIndex = 0;
    private DimensionGeneratorSettings outputSettings;

    public ConfigScreen(CreateWorldScreen parent, DimensionGeneratorSettings settings) {
        this.inputSettings = settings;
        this.outputSettings = settings;
        this.parent = parent;
        this.instance = new Instance(getInitialSettings(settings));
        this.preview = new PreviewPage(instance.settings, getSeed(parent));
        this.pages = new Page[]{
                new PresetsPage(instance, preview, preview.getPreviewWidget()),
                new WorldPage(instance, preview),
                new SimplePreviewPage(GuiKeys.CLIMATE_SETTINGS, "climate", preview, instance, s -> s.climate),
                new SimplePreviewPage(GuiKeys.TERRAIN_SETTINGS, "terrain", preview, instance, s -> s.terrain),
                new SimplePreviewPage(GuiKeys.RIVER_SETTINGS, "rivers", preview, instance, s -> s.rivers),
                new SimplePreviewPage(GuiKeys.FILTER_SETTINGS, "filters", preview, instance, s -> s.filters),
                new SimplePage(GuiKeys.MISC_SETTINGS, "miscellaneous", instance, s -> s.miscellaneous)
        };
    }

    private boolean isPresetsPage() {
        return pages[pageIndex] instanceof PresetsPage;
    }

    @Override
    public void init() {
        super.buttons.clear();
        super.children.clear();

        int buttonsCenter = width / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonPad = 2;
        int buttonsRow = height - 25;

        if (pageIndex < pages.length) {
            Page page = pages[pageIndex];
            TFLabel title = new TFLabel(page.getTitle());
            title.visible = true;
            title.x = 16;
            title.y = 15;
            buttons.add(title);

            try {
                page.initPage(10, 30, this);
                preview.initPage(10, 30, this);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }

        // -52
        addButton(new Button(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.CANCEL.getText(), b -> closeScreen()));

        // +2
        addButton(new Button(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, GuiKeys.DONE.getText(), b -> {
            for (Page page : pages) {
                page.save();
            }

            Log.debug("Updating generator settings...");
            TerraSettings settings = instance.copySettings();
            DynamicRegistries registries = parent.field_238934_c_.func_239055_b_();
            outputSettings = LevelType.updateOverworld(inputSettings, registries, settings);

            Log.debug("Updating seed...");
            ConfigScreen.setSeed(parent, preview.getSeed());
            closeScreen();
        }));

        // -106
        addButton(new Button(buttonsCenter - (buttonWidth * 2 + (buttonPad * 3)), buttonsRow, buttonWidth, buttonHeight, new StringTextComponent("<<"), NO_ACTION) {
            @Override
            public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
                super.active = hasPrevious();
                super.render(matrixStack, mouseX, mouseY, partialTicks);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                if (hasPrevious()) {
                    pageIndex--;
                    init();
                }
            }
        });

        // +56
        addButton(new Button(buttonsCenter + buttonWidth + (buttonPad * 3), buttonsRow, buttonWidth, buttonHeight, new StringTextComponent(">>"), NO_ACTION) {
            @Override
            public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
                super.active = hasNext();
                super.render(matrixStack, mouseX, mouseY, partialTicks);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                if (hasNext()) {
                    pageIndex++;
                    init();
                }
            }
        });

        super.init();
    }

    @Override
    public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        super.renderBackground(matrixStack);
        pages[pageIndex].visit(pane -> pane.render(matrixStack, mouseX, mouseY, partialTicks));

        if (pageIndex > 0) {
            preview.visit(pane -> pane.render(matrixStack, mouseX, mouseY, partialTicks));
        }

        super.render(matrixStack, mouseX, mouseY, partialTicks); }

    @Override
    public void renderOverlays(MatrixStack matrixStack, Screen screen, int mouseX, int mouseY) {
        super.renderOverlays(matrixStack, screen, mouseX, mouseY);
        pages[pageIndex].visit(pane -> pane.renderOverlays(matrixStack, screen, mouseX, mouseY));
        if (!isPresetsPage()) {
            preview.visit(pane -> pane.renderOverlays(matrixStack, screen, mouseX, mouseY));
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseClicked(x, y, button));
        boolean b = isPresetsPage() || preview.action(pane -> pane.mouseClicked(x, y, button));
        boolean c = preview.getPreviewWidget().click(x, y);
        boolean d = super.mouseClicked(x, y, button);
        return a || b || c || d;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseReleased(x, y, button));
        boolean b = isPresetsPage() || preview.action(pane -> pane.mouseReleased(x, y, button));
        boolean c = super.mouseReleased(x, y, button);
        return a || b || c;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseDragged(x, y, button, dx, dy));
        boolean b = isPresetsPage() || preview.action(pane -> pane.mouseDragged(x, y, button, dx, dy));
        boolean c = super.mouseDragged(x, y, button, dx, dy);
        return a || b || c;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseScrolled(x, y, direction));
        boolean b = isPresetsPage() || preview.action(pane -> pane.mouseScrolled(x, y, direction));
        boolean c = super.mouseScrolled(x, y, direction);
        return a || b || c;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        boolean a = pages[pageIndex].action(pane -> pane.keyPressed(i, j, k));
        boolean b = isPresetsPage() || preview.action(pane -> pane.keyPressed(i, j, k));
        boolean c = super.keyPressed(i, j, k);
        return a || b || c;
    }

    @Override
    public boolean charTyped(char ch, int code) {
        boolean a = pages[pageIndex].action(pane -> pane.charTyped(ch, code));
        boolean b = isPresetsPage() || preview.action(pane -> pane.charTyped(ch, code));
        boolean c = super.charTyped(ch, code);
        return a || b || c;
    }

    @Override
    public void closeScreen() {
        Log.debug("Returning to parent screen");
        for (Page page : pages) {
            page.close();
        }
        preview.close();
        Minecraft.getInstance().displayGuiScreen(parent);
        parent.field_238934_c_.func_239043_a_(outputSettings);
    }

    private boolean hasNext() {
        return pageIndex + 1 < pages.length;
    }

    private boolean hasPrevious() {
        return pageIndex > 0;
    }

    protected static int getSeed(CreateWorldScreen screen) {
        TextFieldWidget field = getWidget(screen);
        if (field != null && !field.getText().isEmpty()) {
            try {
                long seed = Long.parseLong(field.getText());
                return (int) seed;
            } catch (NumberFormatException var6) {
                return field.getText().hashCode();
            }
        }
        return -1;
    }

    protected static TerraSettings getInitialSettings(DimensionGeneratorSettings level) {
        if (level.func_236225_f_() instanceof TFChunkGenerator) {
            TerraContext context = ((TFChunkGenerator) level.func_236225_f_()).getContext();
            TerraSettings settings = context.terraSettings;
            TerraSettings copy = new TerraSettings(settings.world.seed);
            JsonElement dataCopy = DataUtils.toJson(settings);
            DataUtils.fromJson(dataCopy, copy);
            return copy;
        }
        throw new IllegalStateException("Not a TerraForged generator :[");
    }

    protected static void setSeed(CreateWorldScreen screen, int seed) {
        TextFieldWidget field = getWidget(screen);
        if (field != null) {
            field.setText(String.valueOf(seed));
        }
    }

    private static TextFieldWidget getWidget(CreateWorldScreen screen) {
        String message = I18n.format("selectWorld.enterSeed");
        for (IGuiEventListener widget : screen.getEventListeners()) {
            if (widget instanceof TextFieldWidget) {
                TextFieldWidget field = (TextFieldWidget) widget;
                if (field.getMessage().getString().equals(message)) {
                    return field;
                }
            }
        }
        return null;
    }

    public static ConfigScreen create(CreateWorldScreen parent, DimensionGeneratorSettings settings) {
        return new ConfigScreen(parent, settings);
    }
}
