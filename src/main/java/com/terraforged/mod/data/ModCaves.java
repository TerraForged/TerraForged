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

package com.terraforged.mod.data;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.NoiseCave;
import com.terraforged.mod.worldgen.cave.CaveType;
import com.terraforged.noise.Source;
import com.terraforged.noise.util.NoiseUtil;

import static com.terraforged.mod.TerraForged.CAVES;

public interface ModCaves {
    static void register() {
        var seed = new RandSeed(901246, 500_000);
        TerraForged.register(CAVES, "synapse_high", Factory.synapse(seed.next(), 0.75F, 96, 384));
        TerraForged.register(CAVES, "synapse_mid", Factory.synapse(seed.next(), 1.0F, 0, 256));
        TerraForged.register(CAVES, "synapse_low", Factory.synapse(seed.next(), 1.2F, -32, 128));
        TerraForged.register(CAVES, "mega", Factory.mega(seed.next(), 1.0F, -16, 64));
        TerraForged.register(CAVES, "mega_deep", Factory.mega(seed.next(), 1.2F, -32, 48));
    }

    class Factory {
        static NoiseCave mega(int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(200 * scale);
            int networkScale = NoiseUtil.floor(250 * scale);
            int floorScale = NoiseUtil.floor(50 * scale);
            int size = NoiseUtil.floor(30 *  scale);

            var elevation = Source.simplex(++seed, elevationScale, 2).map(0.3, 0.7);
            var shape = Source.simplex(++seed, networkScale, 3)
                    .bias(-0.5).abs().scale(2).invert()
                    .clamp(0.75, 1.0).map(0, 1);

            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.3).map(0, 1);

            return new NoiseCave(seed, CaveType.UNIQUE, elevation, shape, floor, size, minY, maxY);
        }

        static NoiseCave synapse(int seed, float scale, int minY, int maxY) {
            int elevationScale = NoiseUtil.floor(350 * scale);
            int networkScale = NoiseUtil.floor(180 * scale);
            int networkWarpScale = NoiseUtil.floor(20 * scale);
            int networkWarpStrength = networkWarpScale / 2;
            int floorScale = NoiseUtil.floor(30 * scale);
            int size = NoiseUtil.floor(15 *  scale);

            var elevation = Source.simplex(++seed, elevationScale, 3).map(0.1, 0.9);
            var shape = Source.simplexRidge(++seed, networkScale, 3)
                    .warp(++seed, networkWarpScale, 1, networkWarpStrength)
                    .clamp(0.35, 0.75).map(0, 1);
            var floor = Source.simplex(++seed, floorScale, 2).clamp(0.0, 0.15).map(0, 1);
            return new NoiseCave(seed, CaveType.GLOBAL, elevation, shape, floor, size, minY, maxY);
        }

        static NoiseCave[] getDefaults() {
            var seed = new RandSeed(901246, 500_000);
            return new NoiseCave[] {
                    Factory.synapse(seed.next(), 0.75F, 96, 384),
                    Factory.synapse(seed.next(), 1.0F, 0, 256),
                    Factory.synapse(seed.next(), 1.2F, -32, 128),
                    Factory.mega(seed.next(), 1.0F, -16, 64),
                    Factory.mega(seed.next(), 1.2F, -32, 48)
            };
        }
    }
}
