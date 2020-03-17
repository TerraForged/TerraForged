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

import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.Dynamic;
import com.terraforged.core.settings.Settings;
import com.terraforged.mod.gui.element.TerraLabel;
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
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Util;
import net.minecraft.world.level.LevelGeneratorOptions;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

public class SettingsScreen extends OverlayScreen {

    private static final ButtonWidget.PressAction NO_ACTION = b -> {
    };

    private final Page[] pages;
    private final CreateWorldScreen parent;
    private final PreviewPage preview = new PreviewPage();
    private final TerraSettings settings = new TerraSettings();

    private int pageIndex = 0;

    public SettingsScreen(CreateWorldScreen parent) {
        // todo enhance this!
        NBTHelper.deserialize((CompoundTag) parent.generatorOptions.getDynamic().convert(NbtOps.INSTANCE).getValue(), settings);
        this.parent = parent;
        this.pages = new Page[]{
                new GeneratorPage(settings, preview),
                new TerrainPage(settings, preview),
                new RiverPage(settings, preview),
                new FilterPage(settings, preview),
                new FeaturePage(settings),
                new StructurePage(settings)
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
        addButton(new ButtonWidget(buttonsCenter - buttonWidth - buttonPad, buttonsRow, buttonWidth, buttonHeight, "Cancel"
                , b -> onClose()));

        // +2
        addButton(new ButtonWidget(buttonsCenter + buttonPad, buttonsRow, buttonWidth, buttonHeight, "Done", b -> {
            for (Page page : pages) {
                page.save();
            }
            LevelGeneratorOptions oldOptions = parent.generatorOptions;
            parent.generatorOptions = new LevelGeneratorOptions(oldOptions.getType(), new Dynamic<>(NbtOps.INSTANCE, NBTHelper.serializeCompact(settings)), oldOptions::createChunkGenerator);
            onClose();
        }));


        // -106
        addButton(new ButtonWidget(buttonsCenter - (buttonWidth * 2 + (buttonPad * 3)), buttonsRow, buttonWidth,
                buttonHeight, "<<", NO_ACTION) {
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

            private boolean hasPrevious() {
                return pageIndex > 0;
            }
        });

        // +56
        addButton(new ButtonWidget(buttonsCenter + buttonWidth + (buttonPad * 3), buttonsRow, buttonWidth, buttonHeight,
                ">>", NO_ACTION) {
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

            private boolean hasNext() {
                return pageIndex + 1 < pages.length;
            }
        });

        addButton(new ButtonWidget(width - buttonWidth - 15, buttonsRow, buttonWidth, buttonHeight, "Export", NO_ACTION) {
            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                export(settings);
            }
        });

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
    public void onClose() {
        for (Page page : pages) {
            page.close();
        }
        preview.close();
        MinecraftClient.getInstance().openScreen(parent);
    }

    private void export(Settings settings) {
        for (Page page : pages) {
            page.save();
        }
        CompoundTag tag = NBTHelper.serializeCompact(settings);
        JsonElement json = NBTHelper.toJson(tag);
        File config = new File(MinecraftClient.getInstance().runDirectory, "config");
        File file = new File(config, SettingsHelper.SETTINGS_FILE_NAME);
        try (Writer writer = new BufferedWriter(new FileWriter(file))) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
            Util.getOperatingSystem().open(file.getParentFile().toURI());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
