package com.terraforged.gui;

import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

public class Instance {

    public TerraSettings settings = new TerraSettings();
    public CompoundNBT settingsData = new CompoundNBT();

    public Instance(TerraSettings settings) {
        sync(settings);
    }

    public void sync(TerraSettings settings) {
        this.settings = settings;
        this.settingsData = NBTHelper.serialize(settings);
    }
}
