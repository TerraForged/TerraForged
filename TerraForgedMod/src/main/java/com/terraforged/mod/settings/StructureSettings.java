/*
 *
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

package com.terraforged.mod.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;

@Serializable
public class StructureSettings {

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between villages")
    public int villageDistance = 2;

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between mansions")
    public int mansionDistance = 3;

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between strongholds")
    public int strongholdDistance = 3;

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between strongholds")
    public int strongholdSpread = 3;

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between biome structures")
    public int biomeStructureDistance = 4;

    @Range(min = 1, max = 10)
    @Comment("Controls the distance between ocean monuments")
    public int oceanMonumentSpacing = 3;

    @Range(min = 1, max = 10)
    @Comment("Controls the separation between ocean monuments")
    public int oceanMonumentSeparation = 3;
}
