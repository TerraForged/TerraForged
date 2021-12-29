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

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.platform.ClientAPI;
import com.terraforged.mod.worldgen.GeneratorPreset;
import net.minecraft.Util;
import net.minecraft.client.gui.components.CycleButton;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.worldselection.CreateWorldScreen;
import net.minecraft.client.gui.screens.worldselection.WorldPreset;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

import java.util.function.Predicate;

public class ScreenUtil {
    private static final String WORLD_TYPE = "selectWorld.mapType";
    private static final Predicate<String> TF_PRESET = s -> s.equals(GeneratorPreset.TRANSLATION_KEY);
    private static final Predicate<String> DEFAULT_PRESET = s -> s.equals(GeneratorPreset.TRANSLATION_KEY);

    public static void enforceDefaultPreset(CreateWorldScreen screen, String name) {
        var button = getPresetButton(screen);
        if (button == null) return;

        var start = button.getValue();
        var keyPredicate = createKeyPredicate(name);

        while (!isPresetSelected(button, keyPredicate)) {
            button.onPress();
            // Stop if we've looped all the way around
            if (button.getValue() == start) return;
        }
    }

    public static boolean isPresetEnabled(CreateWorldScreen screen) {
        var button = getPresetButton(screen);
        if (button == null) return false;

        if (!ClientAPI.get().hasPreset()) return isPresetSelected(button, DEFAULT_PRESET);

        return isPresetSelected(button, TF_PRESET);
    }

    private static Predicate<String> createKeyPredicate(String name) {
        if (name.equals(TerraForged.MODID)) return TF_PRESET;

        var location = new ResourceLocation(name);
        var key = Util.makeDescriptionId("generator", location);

        if (location.getNamespace().equals("minecraft")) {
            var key0 = "generator." + location.getPath();
            return s -> s.equals(key) || s.equals(key0);
        }

        return s -> s.equals(key);
    }

    private static boolean isPresetSelected(CycleButton<?> button, Predicate<String> predicate) {
        var preset = (WorldPreset) button.getValue();
        if (preset.description() instanceof TranslatableComponent description) {
            return predicate.test(description.getKey());
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
