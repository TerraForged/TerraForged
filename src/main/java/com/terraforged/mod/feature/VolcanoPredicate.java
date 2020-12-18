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

package com.terraforged.mod.feature;

import com.terraforged.engine.cell.Cell;
import com.terraforged.engine.tile.chunk.ChunkReader;
import com.terraforged.engine.world.heightmap.Levels;
import com.terraforged.mod.chunk.TFChunkGenerator;
import com.terraforged.mod.featuremanager.predicate.FeaturePredicate;
import com.terraforged.noise.Module;
import com.terraforged.noise.Source;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;

public class VolcanoPredicate implements FeaturePredicate {

    private final Module threshold;
    private final TFChunkGenerator generator;

    public VolcanoPredicate(TFChunkGenerator generator) {
        Levels levels = generator.getContext().levels;
        this.generator = generator;
        this.threshold = Source.perlin(235, 75, 1).scale(levels.scale(32)).bias(levels.ground(24));
    }

    @Override
    public boolean test(IChunk chunk, Biome biome) {
        try (ChunkReader reader = generator.getChunkReader(chunk.getPos())) {
            Cell cell = reader.getCell(8, 8);
            if (cell.terrain.isVolcano()) {
                return testHeight(reader, cell);
            }
        }
        return true;
    }

    private boolean testHeight(ChunkReader chunk, Cell cell) {
        int x = chunk.getBlockX() + 8;
        int z = chunk.getBlockZ() + 8;
        return cell.value < threshold.getValue(x, z);
    }
}
