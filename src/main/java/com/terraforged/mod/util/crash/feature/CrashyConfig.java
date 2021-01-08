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

package com.terraforged.mod.util.crash.feature;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DynamicOps;
import com.terraforged.mod.featuremanager.util.codec.Codecs;
import net.minecraft.world.gen.feature.IFeatureConfig;

import java.util.Collections;

public class CrashyConfig implements IFeatureConfig {

    public static final Codec<CrashyConfig> CODEC = Codecs.create(CrashyConfig::encode, CrashyConfig::decode);

    public final CrashType crashType;

    public CrashyConfig(CrashType crashType) {
        this.crashType = crashType;
    }

    public enum CrashType {
        EXCEPTION,
        DEADLOCK,
        SLOW,
        ;
    }

    private static <T> T encode(CrashyConfig config, DynamicOps<T> ops) {
        return ops.createMap(Collections.singletonMap(
                ops.createString("type"),
                ops.createString(config.crashType.name())
        ));
    }

    private static <T> CrashyConfig decode(T t, DynamicOps<T> ops) {
        return new CrashyConfig(CrashType.valueOf(
                ops.getStringValue(t).result().orElse(CrashType.EXCEPTION.name())
        ));
    }
}
