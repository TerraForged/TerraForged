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

package com.terraforged.mod.gui.preview;

import com.terraforged.core.settings.Settings;
import com.terraforged.mod.gui.OverlayScreen;
import com.terraforged.mod.gui.element.TerraButton;
import com.terraforged.mod.gui.page.BasePage;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;

public class PreviewPage extends BasePage {

    private final Preview preview;
    private final Settings settings = new Settings();
    private final CompoundNBT previewerSettings = NBTHelper.serialize(new PreviewSettings());

    public PreviewPage(int seed) {
        preview = new Preview(seed);
    }

    public int getSeed() {
        return preview.getSeed();
    }

    public void apply(Consumer<Settings> consumer) {
        consumer.accept(settings);
        preview.update(settings, previewerSettings);
    }

    @Override
    public void close() {
        preview.close();
    }

    @Override
    public void save() {

    }

    @Override
    public void init(OverlayScreen parent) {
        Column right = getColumn(1);
        preview.x = 0;
        preview.y = 0;
        preview.setWidth(256);
        preview.setHeight(256);

        addElements(right.left, right.top, right, previewerSettings, right.scrollPane::addButton, this::update);
        right.scrollPane.addButton(new TerraButton("New Seed") {
            @Override
            public void onPress() {
                preview.regenerate();
                update();
            }
        });

        right.scrollPane.addButton(preview);

        // used to pad the scroll-pane out so that the preview legend scrolls on larger gui scales
        TerraButton spacer = createSpacer();
        for (int i = 0; i < 6; i++) {
            right.scrollPane.addButton(spacer);
        }

        update();
    }

    protected void update() {
        preview.update(settings, previewerSettings);
    }

    private static TerraButton createSpacer() {
        return new TerraButton("") {
            @Override
            public void render(int x, int y, float tick) { }
        };
    }
}
