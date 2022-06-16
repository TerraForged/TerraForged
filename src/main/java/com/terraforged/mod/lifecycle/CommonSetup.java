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

package com.terraforged.mod.lifecycle;

import com.terraforged.mod.Environment;
import com.terraforged.mod.TerraForged;
import com.terraforged.mod.worldgen.Generator;
import com.terraforged.mod.worldgen.biome.Source;
import com.terraforged.mod.worldgen.datapack.DataPackExporter;
import net.minecraft.core.Registry;

public class CommonSetup extends Stage {
    public static final CommonSetup STAGE = new CommonSetup();

    CommonSetup() {}

    @Override
    protected void doInit() {
        TerraForged.LOG.info("Registering world-gen core codecs");
        Registry.register(Registry.BIOME_SOURCE, TerraForged.location("climate"), Source.CODEC);
        Registry.register(Registry.CHUNK_GENERATOR, TerraForged.location("generator"), Generator.CODEC);

//        Registry.register(Registry.CHUNK_GENERATOR, TerraForged.location("profiler"), GeneratorProfiler.CODEC);

        if (!Environment.DATA_GEN) {
            DataPackExporter.extractDefaultPack();
        }
    }
}
