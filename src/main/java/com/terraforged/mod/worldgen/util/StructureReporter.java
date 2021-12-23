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

package com.terraforged.mod.worldgen.util;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.Init;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.feature.StructureFeature;

import java.util.Comparator;
import java.util.function.Supplier;

public class StructureReporter extends Init {
    protected final Supplier<NoiseGeneratorSettings> settings;

    public StructureReporter(Supplier<NoiseGeneratorSettings> settings) {
        this.settings = settings;
    }

    @Override
    protected void doInit() {
        TerraForged.LOG.info("Validating structure configs:");
        var structureSettings = settings.get().structureSettings();

        structureSettings.structureConfig().keySet().stream()
                .sorted(Comparator.comparing(StructureFeature::getFeatureName))
                .filter(s -> s == StructureFeature.STRONGHOLD)
                .filter(s -> structureSettings.structures(s).isEmpty())
                .forEach(structure -> {
                    var name = structure.getFeatureName();
                    var mod = getNamespace(name);
                    TerraForged.LOG.error("- [{}] Invalid - Structure has not been assigned to any biomes so it cannot generate. Mod: {}", name, mod);
                });
    }

    private static String getNamespace(String name) {
        int colon = name.indexOf(':');
        return colon > -1 ? name.substring(0, colon) : "unknown";
    }
}
