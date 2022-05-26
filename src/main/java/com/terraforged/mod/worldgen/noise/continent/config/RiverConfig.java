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

package com.terraforged.mod.worldgen.noise.continent.config;

public class RiverConfig {
    public float erosion = 0.075f;
    public final FloatRange bedWidth = new FloatRange(1, 7);
    public final FloatRange bankWidth = new FloatRange(4, 30);
    public final FloatRange valleyWidth = new FloatRange(75, 150);

    public final FloatRange bedDepth = new FloatRange(1.25f, 5f);
    public final FloatRange bankDepth = new FloatRange(1.25f, 3f);

    public RiverConfig copy(RiverConfig config) {
        erosion = config.erosion;

        bedDepth.copy(config.bedDepth);
        bankDepth.copy(config.bankDepth);

        bedWidth.copy(config.bedWidth);
        bankWidth.copy(config.bankWidth);
        valleyWidth.copy(config.valleyWidth);

        return this;
    }

    public RiverConfig scale(float frequency) {
        bedWidth.scale(frequency);
        bankWidth.scale(frequency);
        valleyWidth.scale(frequency);
        return this;
    }

    public static RiverConfig lake() {
        var config = new RiverConfig();
        config.bankWidth.min = 0.55f;
        config.bankWidth.max = 0.55f;

        config.bedWidth.min = 0.02f;
        config.bedWidth.max = 0.05f;

        config.bankDepth.min = 2f;
        config.bankDepth.max = 3f;

        config.valleyWidth.min = 0.6f;
        config.valleyWidth.max = 0.6f;

        return config;
    }
}
