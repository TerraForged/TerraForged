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

package com.terraforged.material.geology;

import com.terraforged.api.material.geology.StrataConfig;
import com.terraforged.api.material.geology.StrataGenerator;
import com.terraforged.material.Materials;
import com.terraforged.world.geology.Strata;
import me.dags.noise.Source;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

public class GeoGenerator implements StrataGenerator {

    private final List<Block> rock;
    private final List<Block> soil;
    private final List<Block> clay;
    private final List<Block> sediment;
    private final List<Source> types = new ArrayList<>();

    public GeoGenerator(Materials materials) {
        types.add(Source.PERLIN);
        rock = Materials.toList(materials.stone);
        soil = Materials.toList(materials.dirt);
        clay = Materials.toList(materials.clay);
        sediment = Materials.toList(materials.sediment);
    }

    @Override
    public Strata<BlockState> generate(int seed, int scale, StrataConfig config) {
        Random random = new Random();
        Strata.Builder<BlockState> builder = Strata.builder(++seed, Source.build(++seed, scale, 3));
        addLayer(seed + 1, random, config.soil, soil, builder);
        addLayer(seed + 2, random, config.sediment, sediment, builder);
        addLayer(seed + 3, random, config.clay, clay, builder);
        addLayer(seed + 4, random, config.rock, rock, builder);
        return builder.build();
    }

    private void addLayer(int seed, Random random, StrataConfig.Config config, List<Block> materials, Strata.Builder<BlockState> builder) {
        random.setSeed(seed);
        List<Layer> layers = generateLayers(materials, config, random);
        layers.forEach(l -> builder.add(l.type, l.state, l.depth));
    }

    private List<Layer> generateLayers(List<Block> materials, StrataConfig.Config config, Random random) {
        int lastIndex = -1;
        int layers = config.getLayers(random.nextFloat());
        List<Layer> result = new ArrayList<>();
        for (int i = 0; i < layers; i++) {
            int attempts = 3;
            int index = random.nextInt(materials.size());
            while (--attempts >= 0 && index == lastIndex) {
                index = random.nextInt(materials.size());
            }
            if (index != lastIndex) {
                lastIndex = index;
                BlockState material = materials.get(index).getDefaultState();
                float depth = config.getDepth(random.nextFloat());
                Source type = nextType(random);
                result.add(new Layer(material, depth, type));
            }
        }
        return result;
    }

    private Source nextType(Random random) {
        int index = random.nextInt(types.size());
        return types.get(index);
    }

    private List<Layer> sortHardness(List<Layer> layers) {
        layers.sort(Comparator.comparing(s -> Materials.getHardness(s.state)));
        return layers;
    }

    private static class Layer {

        private final BlockState state;
        private final float depth;
        private final Source type;

        private Layer(BlockState state, float depth, Source type) {
            this.state = state;
            this.depth = depth;
            this.type = type;
        }
    }
}
