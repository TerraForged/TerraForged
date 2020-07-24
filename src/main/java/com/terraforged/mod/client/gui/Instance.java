package com.terraforged.mod.client.gui;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
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

    public TerraSettings createCopy() {
        TerraSettings settings = new TerraSettings();
        NBTHelper.deserialize(settingsData, settings);
        return settings;
    }
}
