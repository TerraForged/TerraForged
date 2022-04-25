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

import com.terraforged.engine.Seed;
import com.terraforged.engine.settings.WorldSettings;
import com.terraforged.mod.worldgen.noise.continent.ContinentGenerator;
import com.terraforged.mod.worldgen.noise.continent.ContinentNoise0;
import com.terraforged.noise.Module;
import com.terraforged.noise.util.N2DUtil;

import java.awt.*;

public class ContinentPreview {
    public static void main(String[] args) {
        var settings = new WorldSettings.Continent();
        settings.continentNoiseOctaves = 3;

        var continent = new ContinentGenerator(14231, 0.75f, 0.65f, 0.1f);

        var frame = N2DUtil.display(2000, 1000, (x, y, img) -> {
            double freq = 1.0 / settings.continentScale;

            var warp = ContinentNoise0.createWarp(new Seed(13213), settings.continentScale, settings);

            var noise = new Noise(continent)
                    .freq(freq, freq)
                    .warp(warp)
                    .freq(8, 8);

            float value = noise.getValue(x, y);

            return Color.HSBtoRGB(0, 0, value);
        });

        frame.setVisible(true);
    }

    public record Noise(ContinentGenerator continent) implements Module {
        @Override
        public float getValue(float x, float y) {
            float land = continent.getEdgeValue(x, y);
            float river = 1;// continent.getRiverValue(x, y);
            return Math.min(land, river);
        }
    }
}
