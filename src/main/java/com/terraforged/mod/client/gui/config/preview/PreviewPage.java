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

package com.terraforged.mod.client.gui.config.preview;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.terraforged.mod.chunk.settings.TerraSettings;
import com.terraforged.mod.client.gui.GuiKeys;
import com.terraforged.mod.client.gui.config.OverlayScreen;
import com.terraforged.mod.client.gui.config.page.UpdatablePage;
import com.terraforged.mod.client.gui.element.TerraButton;
import com.terraforged.mod.util.nbt.NBTHelper;
import net.minecraft.nbt.CompoundNBT;

import java.util.function.Consumer;

public class PreviewPage extends UpdatablePage {

    private final Preview preview;
    private final TerraSettings settings;
    private final CompoundNBT previewerSettings = NBTHelper.serialize("preview", new PreviewSettings());

    public PreviewPage(TerraSettings settings, int seed) {
        this.preview = new Preview(seed);
        this.settings = settings;
    }

    public Preview getPreviewWidget() {
        return preview;
    }

    public int getSeed() {
        return preview.getSeed();
    }

    @Override
    public void apply(Consumer<TerraSettings> consumer) {
        consumer.accept(settings);
        preview.update(settings, previewerSettings);
    }

    @Override
    public void close() {
        preview.close();
    }

    @Override
    public void init(OverlayScreen parent) {
        Column right = getColumn(1);
        preview.x = 0;
        preview.y = 0;
        preview.setWidth(Preview.SIZE);
        preview.setHeight(Preview.SIZE);

        addElements(right.left, right.top, right, previewerSettings, right.scrollPane::addButton, this::update);

        right.scrollPane.addButton(new TerraButton(GuiKeys.PREVIEW_SEED.get()) {
            @Override
            public void onPress() {
                preview.regenerate();
                update();
            }
        });

        right.scrollPane.addButton(preview);

        // used to pad the scroll-pane out so that the preview legend scrolls on larger gui scales
        TerraButton spacer = createSpacer();
        for (int i = 0; i < 10; i++) {
            right.scrollPane.addButton(spacer);
        }

        update();
    }

    @Override
    public void update() {
        preview.update(settings, previewerSettings);
    }

    private static TerraButton createSpacer() {
        return new TerraButton("") {
            @Override
            public void render(MatrixStack matrixStack, int x, int y, float tick) { }
        };
    }
}
