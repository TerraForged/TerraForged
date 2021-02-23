/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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

package com.terraforged.mod.client.gui.page;

import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.screen.Instance;
import com.terraforged.mod.client.gui.screen.overlay.OverlayScreen;
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
