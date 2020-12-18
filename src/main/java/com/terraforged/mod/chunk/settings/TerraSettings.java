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

package com.terraforged.mod.chunk.settings;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.engine.serialization.annotation.Serializable;
import com.terraforged.engine.settings.Settings;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import com.terraforged.mod.util.nbt.DynamicReader;
import com.terraforged.mod.util.nbt.DynamicWriter;

@Serializable
public class TerraSettings extends Settings {

    public static final Codec<TerraSettings> CODEC = Codecs.create(
            TerraSettings::encode,
            TerraSettings::decode
    );

    public TerraSettings() {

    }

    public TerraSettings(long seed) {
        world.seed = seed;
    }

    public Miscellaneous miscellaneous = new Miscellaneous();

    public DimesionSettings dimensions = new DimesionSettings();

    private static <T> Dynamic<T> encode(TerraSettings settings, DynamicOps<T> ops) {
        return new Dynamic<>(ops, DynamicWriter.serialize(settings, ops, false));
    }

    private static <T> TerraSettings decode(Dynamic<T> dynamic) {
        return DynamicReader.deserialize(new TerraSettings(), dynamic);
    }
}
