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

package com.terraforged.mod;

import com.terraforged.mod.registry.RegistryManager;
import com.terraforged.mod.util.ApiHolder;
import com.terraforged.mod.worldgen.biome.util.matcher.BiomeMatcher;
import com.terraforged.mod.worldgen.biome.util.matcher.BiomeTagMatcher;
import net.minecraft.tags.BiomeTags;

import java.nio.file.Path;
import java.nio.file.Paths;

public interface CommonAPI {
    ApiHolder<CommonAPI> HOLDER = new ApiHolder<>(new CommonAPI() {});

    default Path getContainer() {
        return Paths.get(".");
    }

    default RegistryManager getRegistryManager() {
        return RegistryManager.DEFAULT;
    }

    default BiomeMatcher getOverworldMatcher() {
        return new BiomeTagMatcher.Overworld(BiomeTags.IS_OVERWORLD);
    }

    static CommonAPI get() {
        return HOLDER.get();
    }
}
