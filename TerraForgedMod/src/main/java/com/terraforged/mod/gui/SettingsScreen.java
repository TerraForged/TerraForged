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

import com.terraforged.mod.gui.element.TerraButton;
import com.terraforged.mod.gui.element.TerraLabel;
import com.terraforged.mod.gui.page.ClimatePage;
import com.terraforged.mod.gui.page.DimensionsPage;
import com.terraforged.mod.gui.page.FeaturePage;
import com.terraforged.mod.gui.page.FilterPage;
import com.terraforged.mod.gui.page.GeneratorPage;
import com.terraforged.mod.gui.page.Page;
import com.terraforged.mod.gui.page.RiverPage;
import com.terraforged.mod.gui.page.StructurePage;
import com.terraforged.mod.gui.page.TerrainPage;
import com.terraforged.mod.gui.preview.PreviewPage;
import com.terraforged.mod.settings.SettingsHelper;
import com.terraforged.mod.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
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
    private final TerraSettings settings = new TerraSettings();

    private int pageIndex = 0;
    private boolean hasChanged = false;

    public SettingsScreen(CreateWorldScreen parent) {
        SettingsHelper.applyDefaults(parent.chunkProviderSettingsJson, settings);
        this.parent = parent;
        this.preview = new PreviewPage(settings, getSeed(parent));
        this.pages = new Page[]{
                new GeneratorPage(settings, preview),
                new ClimatePage(settings, preview),
                new TerrainPage(settings, preview),
                new RiverPage(settings, preview),
                new FilterPage(settings, preview),
                new FeaturePage(settings),
                new StructurePage(settings),
                new DimensionsPage(settings),
        };
    }

    @Override
    protected void init() {
        super.buttons.clear();
        super.children.clear();

        int buttonsCenter = width / 2;
        int buttonWidth = 50;
        int buttonHeight = 20;
        int buttonPad = 2;
        int buttonsRow = height - 25;

        if (pageIndex < pages.length) {
            Page page = pages[pageIndex];
            page.callback(() -> this.hasChanged = true);
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
        addButton(new Button(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, "Cancel"
                , b -> onClose()));

        // +2
        addButton(new Button(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, "Done", b -> {
            for (Page page : pages) {
                page.save();
            }
            parent.chunkProviderSettingsJson = NBTHelper.serializeCompact(settings);
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

        addButton(new TerraButton("Set Defaults", "Use the current settings as the defaults", "CTRL + click to clear them") {

            private boolean ctrl = false;

            @Override
            public void render(int mouseX, int mouseY, float partialTicks) {
                if (Screen.hasControlDown()) {
                    if (!ctrl) {
                        ctrl = true;
                        setMessage("Clear Defaults");
                    }
                    super.active = true;
                } else {
                    if (ctrl) {
                        ctrl = false;
                        setMessage("Set Defaults");
                    }
                    super.active = hasChanged;
                }
                super.render(mouseX, mouseY, partialTicks);
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                if (Screen.hasControlDown()) {
                    SettingsHelper.clearDefaults();
                    hasChanged = true;
                } else {
                    for (Page page : pages) {
                        page.save();
                    }
                    hasChanged = false;
                    SettingsHelper.exportDefaults(settings);
                }
            }
        }.init(width - buttonWidth - 55, buttonsRow, 90, buttonHeight));

        super.init();
    }

    @Override
    public void render(int mouseX, int mouseY, float partialTicks) {
        super.renderBackground();
        pages[pageIndex].visit(pane -> pane.render(mouseX, mouseY, partialTicks));
        preview.visit(pane -> pane.render(mouseX, mouseY, partialTicks));
        super.render(mouseX, mouseY, partialTicks);
    }

    @Override
    public void renderOverlays(Screen screen, int mouseX, int mouseY) {
        super.renderOverlays(screen, mouseX, mouseY);
        pages[pageIndex].visit(pane -> pane.renderOverlays(screen, mouseX, mouseY));
        preview.visit(pane -> pane.renderOverlays(screen, mouseX, mouseY));
    }

    @Override
    public boolean mouseClicked(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseClicked(x, y, button));
        boolean b = preview.action(pane -> pane.mouseClicked(x, y, button));
        boolean c = super.mouseClicked(x, y, button);
        return a || b || c;
    }

    @Override
    public boolean mouseReleased(double x, double y, int button) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseReleased(x, y, button));
        boolean b = preview.action(pane -> pane.mouseReleased(x, y, button));
        boolean c = super.mouseReleased(x, y, button);
        return a || b || c;
    }

    @Override
    public boolean mouseDragged(double x, double y, int button, double dx, double dy) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseDragged(x, y, button, dx, dy));
        boolean b = preview.action(pane -> pane.mouseDragged(x, y, button, dx, dy));
        boolean c = super.mouseDragged(x, y, button, dx, dy);
        return a || b || c;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double direction) {
        boolean a = pages[pageIndex].action(pane -> pane.mouseScrolled(x, y, direction));
        boolean b = preview.action(pane -> pane.mouseScrolled(x, y, direction));
        boolean c = super.mouseScrolled(x, y, direction);
        return a || b || c;
    }

    @Override
    public boolean keyPressed(int i, int j, int k) {
        boolean a = pages[pageIndex].action(pane -> pane.keyPressed(i, j, k));
        boolean b = preview.action(pane -> pane.keyPressed(i, j, k));
        boolean c = super.keyPressed(i, j, k);
        return a || b || c;
    }

    @Override
    public boolean charTyped(char c, int code) {
        boolean a = pages[pageIndex].action(pane -> pane.charTyped(c, code));
        boolean b = preview.action(pane -> pane.charTyped(c, code));
        return a || b || super.charTyped(c, code);
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
