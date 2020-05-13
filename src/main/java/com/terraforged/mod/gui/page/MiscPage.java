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

package com.terraforged.mod.gui.page;

import com.terraforged.mod.gui.OverlayScreen;
import com.terraforged.mod.gui.element.TerraLabel;
import com.terraforged.mod.settings.TerraSettings;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

public class MiscPage extends BasePage {

    private final TerraSettings settings;
    private final CompoundNBT filterSettings;
    private final CompoundNBT featureSettings;

    public MiscPage(TerraSettings settings) {
        this.settings = settings;
        this.filterSettings = NBTHelper.serialize("filters", settings.filters);
        this.featureSettings = NBTHelper.serialize("features", settings.features);
    }

    @Override
    public String getTitle() {
        return "Miscellaneous Settings";
    }

    @Override
    public void save() {
        NBTHelper.deserialize(filterSettings, settings.filters);
        NBTHelper.deserialize(featureSettings, settings.features);
    }

    @Override
    public void init(OverlayScreen parent) {
        Column left = getColumn(0);
        addElements(left.left, left.top, left, filterSettings, true, left.scrollPane::addButton, this::update);

        left.scrollPane.addButton(new TerraLabel("Features & Decorators"));
        addElements(left.left, left.top, left, featureSettings, false, left.scrollPane::addButton, this::update);
    }
}
