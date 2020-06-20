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

package com.terraforged.gui.page;

import com.terraforged.TerraWorld;
import com.terraforged.chunk.settings.TerraSettings;
import com.terraforged.gui.OverlayScreen;
import com.terraforged.gui.element.TerraTextInput;
import com.terraforged.util.nbt.NBTHelper;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.WorldType;
import net.minecraftforge.registries.ForgeRegistries;

public class WorldPage extends BasePage {

    private final TerraSettings settings;
    private final UpdatablePage preview;
    private final CompoundNBT worldSettings;
    private final CompoundNBT dimSettings;

    public WorldPage(TerraSettings settings, UpdatablePage preview) {
        this.settings = settings;
        this.preview = preview;
        this.worldSettings = NBTHelper.serialize("world", settings.world);
        this.dimSettings = NBTHelper.serialize("dimensions", settings.dimensions);

        CompoundNBT generators = dimSettings.getCompound("dimensions").getCompound("value");
        for (String name : generators.keySet()) {
            CompoundNBT setting = generators.getCompound(name);
            setting.put("#options", getWorldTypes());
        }
    }

    @Override
    public String getTitle() {
        return "World Settings";
    }

    @Override
    public void save() {
        NBTHelper.deserialize(worldSettings, settings.world);
        NBTHelper.deserialize(dimSettings, settings.dimensions);
    }

    @Override
    public void init(OverlayScreen parent) {
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

    private static ListNBT getWorldTypes() {
        ListNBT options = new ListNBT();
        for (WorldType type : WorldType.WORLD_TYPES) {
            if (type == null || (type.getId() >= 1 && type.getId() <= 6) || type.getId() == 8) {
                continue;
            }
            if (!TerraWorld.isTerraType(type)) {
                options.add(StringNBT.valueOf(type.getName()));
            }
        }
        return options;
    }
}
