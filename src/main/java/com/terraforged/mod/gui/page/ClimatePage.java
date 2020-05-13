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

import com.terraforged.core.settings.Settings;
import com.terraforged.mod.gui.OverlayScreen;
import com.terraforged.mod.gui.preview.PreviewPage;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

public class ClimatePage extends BasePage {

    private final Settings settings;
    private final PreviewPage preview;
    private final CompoundNBT climateSettings;

    public ClimatePage(Settings settings, PreviewPage preview) {
        this.settings = settings;
        this.preview = preview;
        this.climateSettings = NBTHelper.serialize("climate", settings.climate);
    }

    @Override
    public String getTitle() {
        return "Climate Settings";
    }

    @Override
    public void save() {
        NBTHelper.deserialize(climateSettings, settings.climate);
    }

    @Override
    public void init(OverlayScreen parent) {
        Column center = getColumn(0);
        center.scrollPane.setScrollAmount(0D);
        addElements(0, 0, center, climateSettings, true, center.scrollPane::addButton, this::update);
    }

    @Override
    protected void update() {
        super.update();
        preview.apply(settings -> NBTHelper.deserialize(climateSettings, settings.climate));
    }
}
