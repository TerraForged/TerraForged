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

import com.terraforged.engine.world.terrain.TerrainType;
import com.terraforged.mod.worldgen.asset.TerrainNoise;
import com.terraforged.mod.worldgen.terrain.TerrainBlender;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.N2DUtil;

import java.awt.*;

public class TerrainBlenderTest {
    public static void main(String[] args) {
        var blender = new TerrainBlender(123, 80, 0.7F, 0.75F, new TerrainNoise[]{
                new TerrainNoise(TerrainType.FLATS, 1F, Source.constant(0)),
                new TerrainNoise(TerrainType.FLATS, 1F, Source.constant(1)),
        });

        N2DUtil.display(1000, 800, (x, z, img) -> {
            float noise = blender.getValue(x, z);
            img.setRGB(x, z, Color.HSBtoRGB(0, 0, noise));
        }).setVisible(true);
    }
}
