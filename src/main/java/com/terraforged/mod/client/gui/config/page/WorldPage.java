/*
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

package com.terraforged.mod.client.gui.config.page;

import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.config.Instance;
import com.terraforged.mod.client.gui.config.OverlayScreen;
import com.terraforged.mod.client.gui.element.TerraTextInput;
import com.terraforged.mod.client.gui.page.BasePage;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;

public class WorldPage extends BasePage {

    private final UpdatablePage preview;
    private final Instance instance;

    private CompoundNBT worldSettings = null;
    private CompoundNBT dimSettings = null;

    public WorldPage(Instance instance, UpdatablePage preview) {
        this.instance = instance;
        this.preview = preview;
    }

    @Override
    public String getTitle() {
        return GuiKeys.WORLD_SETTINGS.get();
    }

    @Override
    public void save() {

    }

    @Override
    public void init(OverlayScreen parent) {
        // re-sync settings from the settings object to the data structure
        worldSettings = getWorldSettings();
        dimSettings = getDimSettings();

        Column left = getColumn(0);
        addElements(left.left, left.top, left, worldSettings, true, left.scrollPane::addButton, this::update);

        addElements(left.left, left.top, left, dimSettings, true, left.scrollPane::addButton, this::update);
    }

    @Override
    public void onAddWidget(Widget widget) {
        if (widget instanceof TerraTextInput) {
            TerraTextInput input = (TerraTextInput) widget;
            input.setColorValidator(string -> ForgeRegistries.BLOCKS.containsKey(new ResourceLocation(string)));
        }
    }

    protected void update() {
        super.update();
        preview.apply(settings -> {
            NBTHelper.deserialize(worldSettings, settings.world);
            NBTHelper.deserialize(dimSettings, settings.dimensions);
        });
    }

    private CompoundNBT getWorldSettings() {
        return instance.settingsData.getCompound("world");
    }

    private CompoundNBT getDimSettings() {
        CompoundNBT dimSettings = instance.settingsData.getCompound("dimensions");
        CompoundNBT generators = dimSettings.getCompound("dimensions");
        for (String name : generators.keySet()) {
            if (name.startsWith("#")) {
                CompoundNBT metadata = generators.getCompound(name);
                metadata.put("options", getWorldTypes());
            }
        }
        return dimSettings;
    }

    private static ListNBT getWorldTypes() {
        ListNBT options = new ListNBT();
//        for (WorldType type : WorldType.WORLD_TYPES) {
//            if (type == null || (type.getId() >= 1 && type.getId() <= 6) || type.getId() == 8) {
//                continue;
//            }
//            if (!TerraWorld.isTerraType(type)) {
//                options.add(StringNBT.valueOf(type.getName()));
//            }
//        }
        return options;
    }
}
