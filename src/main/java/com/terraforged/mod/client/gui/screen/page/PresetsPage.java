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

package com.terraforged.mod.client.gui.screen.page;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.chunk.settings.preset.PresetManager;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.element.TFButton;
import com.terraforged.mod.client.gui.element.TFLabel;
import com.terraforged.mod.client.gui.element.TFTextBox;
import com.terraforged.mod.client.gui.page.BasePage;
import com.terraforged.mod.client.gui.screen.ConfigScreen;
import com.terraforged.mod.client.gui.screen.Instance;
import com.terraforged.mod.client.gui.screen.ScrollPane;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
import com.terraforged.mod.config.ConfigManager;
import com.terraforged.mod.util.DataUtils;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;

import java.util.Optional;
import java.util.function.Predicate;
import java.util.regex.Pattern;

public class PresetsPage extends BasePage {

    private static final Predicate<String> NAME_VALIDATOR = Pattern.compile("^[A-Za-z0-9\\-_ ]+$").asPredicate();

    private final Instance instance;
    private final UpdatablePage preview;
    private final Widget previewWidget;
    private final TFTextBox nameInput;
    private final ConfigScreen parent;
    private final PresetManager manager = PresetManager.load();

    public PresetsPage(ConfigScreen parent, Instance instance, UpdatablePage preview, Widget widget) {
        CompoundNBT value = new CompoundNBT();
        value.putString("name", "");
        this.parent = parent;
        this.preview = preview;
        this.previewWidget = widget;
        this.instance = instance;
        this.nameInput = new TFTextBox("name", value);
        this.nameInput.setColorValidator(NAME_VALIDATOR);
    }

    @Override
    public String getTitle() {
        return GuiKeys.PRESETS.get();
    }

    @Override
    public void close() {
        manager.saveAll();
    }

    @Override
    public void save() {

    }

    protected void update() {
        preview.apply(settings -> DataUtils.fromNBT(instance.settingsData, settings));
    }

    @Override
    public void init(OverlayScreen parent) {
        rebuildPresetList();

        Column right = getColumn(1);
        right.scrollPane.addButton(nameInput);

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_CREATE.get()) {

            @Override
            public void render(MatrixStack matrixStack, int x, int z, float ticks) {
                // only render as active if the text field is not empty
                super.active = nameInput.isValid();
                super.render(matrixStack, x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                // create new preset with current settings
                Preset preset = new Preset(nameInput.getValue(), instance.copySettings());

                // register with the manager & reset the text field
                manager.add(preset);
                nameInput.setValue("");

                // select newly created preset & load
                setSelected(preset);
                load(preset);

                // update the ui
                rebuildPresetList();
            }
        });

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_LOAD.get()) {

            @Override
            public void render(MatrixStack matrixStack, int x, int z, float ticks) {
                super.active = hasSelectedPreset(false);
                super.render(matrixStack, x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> load(preset));
            }
        });

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_SAVE.get()) {

            @Override
            public void render(MatrixStack matrixStack, int x, int z, float ticks) {
                super.active = hasSelectedPreset(true);
                super.render(matrixStack, x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    if (preset.internal()) {
                        return;
                    }

                    // create a copy of the settings
                    TerraSettings settings = instance.copySettings();

                    // replace the current preset with the updated version
                    manager.add(new Preset(preset.getName(), settings));
                    manager.saveAll();

                    // update the ui
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_RESET.get()) {

            @Override
            public void render(MatrixStack matrixStack, int x, int z, float ticks) {
                super.active = hasSelectedPreset(true);
                super.render(matrixStack, x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    if (preset.internal()) {
                        return;
                    }

                    // create new preset with the same name but default settings
                    Preset reset = new Preset(preset.getName(), new TerraSettings());

                    // replaces by name
                    manager.add(reset);

                    // update the ui
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_DELETE.get()) {

            @Override
            public void render(MatrixStack matrixStack, int x, int z, float ticks) {
                super.active = hasSelectedPreset(true);
                super.render(matrixStack, x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    if (preset.internal()) {
                        return;
                    }
                    // remove & update the ui
                    manager.remove(preset.getName());
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(new TFButton(GuiKeys.PRESET_SET_DEFAULTS.get()) {
            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> ConfigManager.GENERAL.set(cfg -> cfg.set("default_preset", preset.getName())));
            }
        });

        right.scrollPane.addButton(previewWidget);

        // used to pad the scroll-pane out so that the preview legend scrolls on larger gui scales
        TFButton spacer = createSpacer();
        for (int i = 0; i < 10; i++) {
            right.scrollPane.addButton(spacer);
        }
    }

    private boolean hasSelectedPreset(boolean checkEditable) {
        return getSelected().map(preset -> !checkEditable || !preset.internal()).orElse(false);
    }

    private void load(Preset preset) {
        TerraSettings settings = preset.getSettings();
        parent.syncStructureSettings(settings);
        instance.sync(settings);
        update();
    }

    private void setSelected(Preset preset) {
        ScrollPane pane = getColumn(0).scrollPane;
        for (ScrollPane.Entry entry : pane.children()) {
            if (entry.option.getMessage().getString().equalsIgnoreCase(preset.getName())) {
                pane.setSelected(entry);
                return;
            }
        }
    }

    private Optional<Preset> getSelected() {
        ScrollPane.Entry entry = getColumn(0).scrollPane.getSelected();
        if (entry == null) {
            return Optional.empty();
        }
        return manager.get(entry.option.getMessage().getString());
    }

    private void rebuildPresetList() {
        Column left = getColumn(0);
        left.scrollPane.setSelected(null);
        left.scrollPane.setRenderSelection(true);
        left.scrollPane.children().clear();

        for (Preset preset : manager) {
            if (preset.internal()) {
                left.scrollPane.addButton(new TFLabel(preset.getName(), preset.getDescription(), 0xAAAAAA));
            } else {
                left.scrollPane.addButton(new TFLabel(preset.getName()));
            }
        }
    }

    private static TFButton createSpacer() {
        return new TFButton("") {
            @Override
            public void render(MatrixStack matrixStack, int x, int y, float tick) { }
        };
    }
}
