package com.terraforged.mod.client.gui.page;

import com.terraforged.mod.chunk.settings.SettingsHelper;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.chunk.settings.preset.Preset;
import com.terraforged.mod.chunk.settings.preset.PresetManager;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.Instance;
import com.terraforged.mod.client.gui.OverlayScreen;
import com.terraforged.mod.client.gui.ScrollPane;
import com.terraforged.mod.client.gui.element.TerraButton;
import com.terraforged.mod.client.gui.element.TerraLabel;
import com.terraforged.mod.client.gui.element.TerraTextInput;
import com.terraforged.mod.util.nbt.NBTHelper;
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
    private final TerraTextInput nameInput;
    private final PresetManager manager = PresetManager.load();

    public PresetsPage(Instance instance, UpdatablePage preview, Widget widget) {
        CompoundNBT value = new CompoundNBT();
        value.putString("name", "");
        this.preview = preview;
        this.previewWidget = widget;
        this.instance = instance;
        this.nameInput = new TerraTextInput("name", value);
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
        preview.apply(settings -> NBTHelper.deserialize(instance.settingsData, settings));
    }

    @Override
    public void init(OverlayScreen parent) {
        rebuildPresetList();

        Column right = getColumn(1);
        right.scrollPane.addButton(nameInput);

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_CREATE.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                // only render as active if the text field is not empty
                super.active = nameInput.isValid();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                // create new preset with current settings
                Preset preset = new Preset(nameInput.getValue(), instance.createCopy());

                // register with the manager & reset the text field
                manager.add(preset);
                nameInput.setText("");

                // select newly created preset & load
                setSelected(preset);
                load(preset);

                // update the ui
                rebuildPresetList();
            }
        });

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_LOAD.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                super.active = hasSelectedPreset();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> load(preset));
            }
        });

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_SAVE.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                super.active = hasSelectedPreset();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    // create a copy of the settings
                    TerraSettings settings = instance.createCopy();

                    // replace the current preset with the updated version
                    manager.add(new Preset(preset.getName(), settings));

                    // update the ui
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_RESET.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                super.active = hasSelectedPreset();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    // create new preset with the same name but default settings
                    Preset reset = new Preset(preset.getName(), new TerraSettings());

                    // replaces by name
                    manager.add(reset);

                    // update the ui
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_DEFAULT.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                super.active = hasSelectedPreset();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    TerraSettings settings = preset.getSettings();

                    SettingsHelper.exportDefaults(settings);
                });
            }
        });

        right.scrollPane.addButton(new TerraButton(GuiKeys.PRESET_DELETE.get()) {

            @Override
            public void render(int x, int z, float ticks) {
                super.active = hasSelectedPreset();
                super.render(x, z, ticks);
            }

            @Override
            public void onClick(double x, double y) {
                super.onClick(x, y);
                getSelected().ifPresent(preset -> {
                    // remove & update the ui
                    manager.remove(preset.getName());
                    rebuildPresetList();
                });
            }
        });

        right.scrollPane.addButton(previewWidget);

        // used to pad the scroll-pane out so that the preview legend scrolls on larger gui scales
        TerraButton spacer = createSpacer();
        for (int i = 0; i < 7; i++) {
            right.scrollPane.addButton(spacer);
        }
    }

    private boolean hasSelectedPreset() {
        return getColumn(0).scrollPane.getSelected() != null;
    }

    private void load(Preset preset) {
        instance.sync(preset.getSettings());

        update();
    }

    private void setSelected(Preset preset) {
        ScrollPane pane = getColumn(0).scrollPane;
        for (ScrollPane.Entry entry : pane.children()) {
            if (entry.option.getMessage().equalsIgnoreCase(preset.getName())) {
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
        return manager.get(entry.option.getMessage());
    }

    private void rebuildPresetList() {
        Column left = getColumn(0);
        left.scrollPane.setSelected(null);
        left.scrollPane.setRenderSelection(true);
        left.scrollPane.children().clear();

        for (Preset preset : manager) {
            left.scrollPane.addButton(new TerraLabel(preset.getName()));
        }
    }

    private static TerraButton createSpacer() {
        return new TerraButton("") {
            @Override
            public void render(int x, int y, float tick) { }
        };
    }
}
