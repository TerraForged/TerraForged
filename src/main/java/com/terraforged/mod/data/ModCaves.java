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

import com.terraforged.mod.registry.ModRegistries;
import com.terraforged.mod.registry.ModRegistry;
import com.terraforged.mod.util.seed.RandSeed;
import com.terraforged.mod.worldgen.asset.NoiseCave;

interface ModCaves extends ModRegistry {
    static void register() {
        var seed = new RandSeed(901246, 500_000);
        ModRegistries.register(CAVE, "synapse_high", NoiseCave.synapseCave(seed.next(), 1F, 96, 384));
        ModRegistries.register(CAVE, "synapse_mid", NoiseCave.synapseCave(seed.next(), 1.1F, 0, 256));
        ModRegistries.register(CAVE, "synapse_low", NoiseCave.synapseCave(seed.next(), 1.2F, -32, 128));
        ModRegistries.register(CAVE, "mega", NoiseCave.megaCave(seed.next(), 1.0F, -16, 64));
        ModRegistries.register(CAVE, "mega_deep", NoiseCave.megaCave(seed.next(), 1.2F, -32, 48));
    }
}
