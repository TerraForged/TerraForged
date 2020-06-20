package com.terraforged.gui.page;

import com.terraforged.gui.OverlayScreen;
import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Function;

public class SimplePage extends BasePage {

    private final String title;
    protected final TerraSettings settings;
    protected final CompoundNBT sectionSettings;
    protected final Function<TerraSettings, Object> section;

    public SimplePage(String title, String sectionName, TerraSettings settings, Function<TerraSettings, Object> section) {
        this.title = title;
        this.section = section;
        this.settings = settings;
        this.sectionSettings = NBTHelper.serialize(sectionName, section.apply(settings));
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void save() {
        NBTHelper.deserialize(sectionSettings, section);
    }

    @Override
    public void init(OverlayScreen parent) {
        Column left = getColumn(0);
        addElements(left.left, left.top, left, sectionSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    protected void update() {
        super.update();
    }
}
