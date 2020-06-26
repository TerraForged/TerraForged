package com.terraforged.mod.client.gui.page;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.Instance;
import com.terraforged.mod.client.gui.OverlayScreen;
import com.terraforged.mod.util.TranslationKey;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Function;

public class SimplePage extends BasePage {

    private final String title;
    private final String sectionName;
    protected final Instance instance;
    protected final Function<TerraSettings, Object> section;

    protected CompoundNBT sectionSettings = null;

    public SimplePage(TranslationKey title, String sectionName, Instance instance, Function<TerraSettings, Object> section) {
        this.title = title.get();
        this.section = section;
        this.sectionName = sectionName;
        this.instance = instance;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void save() {

    }

    @Override
    public void init(OverlayScreen parent) {
        sectionSettings = getSectionSettings();
        Column left = getColumn(0);
        addElements(left.left, left.top, left, sectionSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    protected void update() {
        super.update();
    }

    private CompoundNBT getSectionSettings() {
        return instance.settingsData.getCompound(sectionName);
    }
}
