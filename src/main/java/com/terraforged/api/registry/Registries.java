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

package com.terraforged.api.registry;

import com.terraforged.api.level.type.LevelType;
import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.RegistryBuilder;

public class Registries {

    public static final CodecRegistry<LevelType> LEVEL_TYPES = CodecRegistry.of(new RegistryBuilder<LevelType>()
            .setName(new ResourceLocation(TerraForgedMod.MODID, "level_type"))
            .setType(LevelType.class)
            .disableSaving()
            .disableSync());

    public static void init() {
        Log.info("Creating level-type registry");
    }
}
