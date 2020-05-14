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

package com.terraforged.settings;

import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;

@Serializable
public class StructureSettings {

    public Structure villages = new Structure(32, 8);

    public Structure mansions = new Structure(80, 20);

    public Structure strongholds = new Structure(32, 3);

    public Structure shipwrecks = new Structure(16, 8);

    public Structure oceanRuins = new Structure(16, 8);

    public Structure oceanMonuments = new Structure(32, 5);

    public Structure otherStructures = new Structure(32, 1);

    @Serializable
    public static class Structure {

        @Range(min = 1, max = 200)
        @Comment("The distance (in chunks) between placements of this feature")
        public int distance;

        @Range(min = 1, max = 50)
        @Comment("The separation (in chunks) between placements of this feature")
        public int separation;

        public Structure() {

        }

        public Structure(int distance, int separation) {
            this.distance = distance;
            this.separation = separation;
        }
    }
}
