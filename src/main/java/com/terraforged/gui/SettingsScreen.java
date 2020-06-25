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

package com.terraforged.gui;

import com.terraforged.chunk.settings.SettingsHelper;
import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.gui.element.TerraLabel;
import com.terraforged.gui.page.Page;
import com.terraforged.gui.page.PresetsPage;
import com.terraforged.gui.page.SimplePage;
import com.terraforged.gui.page.SimplePreviewPage;
import com.terraforged.gui.page.WorldPage;
import com.terraforged.gui.preview.PreviewPage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.CreateWorldScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.resources.I18n;

public class SettingsScreen extends OverlayScreen {

    private static final Button.IPressable NO_ACTION = b -> {};

    private final Page[] pages;
    private final PreviewPage preview;
    private final CreateWorldScreen parent;
    private final Instance instance;

    private int pageIndex = 0;

    public SettingsScreen(CreateWorldScreen parent) {
        TerraSettings settings = new TerraSettings();
        SettingsHelper.applyDefaults(parent.chunkProviderSettingsJson, settings);

        this.parent = parent;
        this.instance = new Instance(settings);
        this.preview = new PreviewPage(instance.settings, getSeed(parent));
        this.pages = new Page[]{
                new PresetsPage(instance, preview, preview.getPreviewWidget()),
                new WorldPage(instance, preview),
                new SimplePreviewPage("Climate Settings", "climate", preview, instance, s -> s.climate),
                new SimplePreviewPage("Terrain Settings", "terrain", preview, instance, s -> s.terrain),
                new SimplePreviewPage("River Settings", "rivers", preview, instance, s -> s.rivers),
                new SimplePreviewPage("Filter Settings", "filters", preview, instance, s -> s.filters),
                new SimplePage("Structure Settings", "structures", instance, s -> s.structures),
                new SimplePage("Feature Settings", "miscellaneous", instance, s -> s.miscellaneous)
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
            TerraLabel title = new TerraLabel(page.getTitle());
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
        addButton(new Button(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, "Cancel", b -> onClose()));

        // +2
        addButton(new Button(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, "Done", b -> {
            for (Page page : pages) {
                page.save();
            }
//            parent.chunkProviderSettingsJson = NBTHelper.serializeCompact(settings);
            SettingsScreen.setSeed(parent, preview.getSeed());
            onClose();
        }));

        // -106
        addButton(new Button(buttonsCenter - (buttonWidth * 2 + (buttonPad * 3)), buttonsRow, buttonWidth, buttonHeight, "<<", NO_ACTION) {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                super.active = hasPrevious();
                super.render(mouseX, mouseY, partialTicks);
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
        addButton(new Button(buttonsCenter + buttonWidth + (buttonPad * 3), buttonsRow, buttonWidth, buttonHeight, ">>", NO_ACTION) {
            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                super.active = hasNext();
                super.render(mouseX, mouseY, partialTicks);
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
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.renderBackground();
        pages[pageIndex].visit(pane -> pane.render(mouseX, mouseY, partialTicks));
        if (pageIndex > 0) {
            preview.visit(pane -> pane.render(mouseX, mouseY, partialTicks));
        }
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderOverlays(Screen screen, int mouseX, int mouseY) {
        super.renderOverlays(screen, mouseX, mouseY);
        pages[pageIndex].visit(pane -> pane.renderOverlays(screen, mouseX, mouseY));
        if (!isPresetsPage()) {
            preview.visit(pane -> pane.renderOverlays(screen, mouseX, mouseY));
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseClicked(x, y, button));
        boolean b =  isPresetsPage() || preview.action(pane -> pane.mouseClicked(x, y, button));;
        boolean c = super.mouseClicked(x, y, button);
        return a || b || c || true;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseReleased(x, y, button));
        boolean b = isPresetsPage() || preview.action(pane -> pane.mouseReleased(x, y, button));
        boolean c = super.mouseReleased(x, y, button);
        return a || b || c || true;
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
    public void onClose() {
        for (Page page : pages) {
            page.close();
        }
        preview.close();
        Minecraft.getInstance().displayGuiScreen(parent);
    }

    private boolean hasNext() {
        return pageIndex + 1 < pages.length;
    }

    private boolean hasPrevious() {
        return pageIndex > 0;
    }

    private static int getSeed(CreateWorldScreen screen) {
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

    private static void setSeed(CreateWorldScreen screen, int seed) {
        TextFieldWidget field = getWidget(screen);
        if (field != null) {
            field.setText("" + seed);
        }
    }

    private static TextFieldWidget getWidget(CreateWorldScreen screen) {
        String message = I18n.format("selectWorld.enterSeed");
        for (IGuiEventListener widget : screen.children()) {
            if (widget instanceof TextFieldWidget) {
                TextFieldWidget field = (TextFieldWidget) widget;
                if (field.getMessage().equals(message)) {
                    return field;
                }
            }
        }
        return null;
    }
}
