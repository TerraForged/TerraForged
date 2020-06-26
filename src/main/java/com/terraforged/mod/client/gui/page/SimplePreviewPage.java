package com.terraforged.mod.client.gui.page;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.Instance;
import com.terraforged.mod.util.TranslationKey;
import com.terraforged.mod.util.nbt.NBTHelper;

import java.util.function.Function;

public class SimplePreviewPage extends SimplePage {

    private final UpdatablePage preview;

    public SimplePreviewPage(TranslationKey title, String sectionName, UpdatablePage preview, Instance instance, Function<TerraSettings, Object> section) {
        super(title, sectionName, instance, section);
        this.preview = preview;
    }

    @Override
    protected void update() {
        super.update();
        preview.apply(settings -> NBTHelper.deserialize(sectionSettings, section.apply(settings)));
    }
}
