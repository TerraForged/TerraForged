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

package com.terraforged.mod.client.screen;

import com.terraforged.mod.platform.ClientAPI;
import com.terraforged.mod.worldgen.GeneratorPreset;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ScreenUtil {
    private static final String WORLD_TYPE = "selectWorld.mapType";

    public static void enforceDefaultPreset(CreateWorldScreen screen) {
        if (!ClientAPI.get().isDefaultPreset()) return;

        var button = getPresetButton(screen);
        if (button == null) return;

        var start = button.getValue();

        while (!isPresetSelected(button, GeneratorPreset.TRANSLATION_KEY)) {
            button.onPress();
            // Stop if we've looped all the way around
            if (button.getValue() == start) return;
        }
    }

    public static boolean isPresetSelected(CreateWorldScreen screen) {
        var button = getPresetButton(screen);
        if (button == null) return false;

        if (!ClientAPI.get().hasPreset()) return isPresetSelected(button, "generator.default");

        return isPresetSelected(button, GeneratorPreset.TRANSLATION_KEY);
    }

    private static boolean isPresetSelected(CycleButton<?> button, String key) {
        var preset = (WorldPreset) button.getValue();
        if (preset.description() instanceof TranslatableComponent description) {
            return description.getKey().equals(key);
        }
        return false;
    }

    private static CycleButton<?> getPresetButton(Screen screen) {
        for (var child : screen.children()) {
            if (child instanceof CycleButton button) {
                if (isPresetButtonText(button.getMessage())) {
                    return button;
                }
            }
        }
        return null;
    }

    private static boolean isPresetButtonText(Component component) {
        if (component instanceof TranslatableComponent translatable) {
            if (translatable.getKey().equals(WORLD_TYPE)) return true;

            for (var arg : translatable.getArgs()) {
                if (arg instanceof Component argText && isPresetButtonText(argText)) return true;
            }
        }
        return false;
    }
}
