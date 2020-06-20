package com.terraforged.gui.page;

import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.util.nbt.NBTHelper;

import java.util.function.Function;

public class SimplePreviewPage extends SimplePage {

    private final UpdatablePage preview;

    public SimplePreviewPage(String title, String sectionName, UpdatablePage preview, TerraSettings settings, Function<TerraSettings, Object> section) {
        super(title, sectionName, settings, section);
        this.preview = preview;
    }

    @Override
    protected void update() {
        super.update();
        preview.apply(settings -> NBTHelper.deserialize(sectionSettings, section.apply(settings)));
    }
}
