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

public class GeneratorPage extends BasePage {

    private final Settings settings;
    private final PreviewPage preview;
    private final CompoundNBT generatorSettings;

    public GeneratorPage(Settings settings, PreviewPage preview) {
        this.settings = settings;
        this.preview = preview;
        this.generatorSettings = NBTHelper.serialize(settings.generator);
    }

    @Override
    public String getTitle() {
        return "Generator Settings";
    }

    @Override
    public void save() {
        NBTHelper.deserialize(generatorSettings, settings.generator);
    }

    @Override
    public void init(OverlayScreen parent) {
        Column left = getColumn(0);
        addElements(left.left, left.top, left, generatorSettings, true, left.scrollPane::addButton, this::update);
    }

    protected void update() {
        super.update();
        preview.apply(settings -> NBTHelper.deserialize(generatorSettings, settings.generator));
    }
}
