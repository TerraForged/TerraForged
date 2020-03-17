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

package com.terraforged.mod.gui.element;

import net.minecraft.client.gui.widget.button.Button;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TerraButton extends Button implements Element {

    private final List<String> tooltip;

    public TerraButton(String displayString) {
        super(0, 0, 200, 20, displayString, b -> {});
        this.tooltip = Collections.emptyList();
    }

    public TerraButton(String displayString, String... tooltip) {
        super(0, 0, 200, 20, displayString, b -> {});
        this.tooltip = Arrays.asList(tooltip);
    }

    public TerraButton init(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        setWidth(width);
        setHeight(height);
        return this;
    }

    @Override
    public List<String> getTooltip() {
        return tooltip;
    }

    public static TerraButton create(String title, int x, int y, int width, int height, String tooltip, Runnable action) {
        TerraButton button = new TerraButton(title) {

            private final List<String> tooltips = Collections.singletonList(tooltip);

            @Override
            public List<String> getTooltip() {
                return tooltips;
            }

            @Override
            public void onClick(double mouseX, double mouseY) {
                super.onClick(mouseX, mouseY);
                action.run();
            }
        };

        button.x = x;
        button.y = y;
        button.setWidth(width);
        button.setHeight(height);

        return button;
    }
}
