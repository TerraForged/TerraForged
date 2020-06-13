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

package com.terraforged.util.setup;

import com.terraforged.Log;
import com.terraforged.api.event.SetupEvent;
import com.terraforged.core.Seed;
import com.terraforged.n2d.Source;
import com.terraforged.n2d.source.Builder;
import com.terraforged.world.geology.Strata;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
public class SetupDebug {

    @SubscribeEvent
    public static void terrain(SetupEvent.Terrain event) {
        log(event);
    }

    @SubscribeEvent
    public static void surface(SetupEvent.Surface event) {
        log(event);
    }

    @SubscribeEvent
    public static void layers(SetupEvent.Layers event) {
        log(event);
    }

    @SubscribeEvent
    public static void geology(SetupEvent.Geology event) {
        Seed seed = event.getContext().seed;

        // Noise parameters used to vary the depth of the strata bands
        Builder noise = Source.build(seed.next(), 150, 1);

        // Set each layer material & its relative depth (top to bottom)
        Strata<BlockState> strata = Strata.<BlockState>builder(seed.next(), noise)
                .add(Blocks.STONE.getDefaultState(), 0.5)
                .add(Blocks.GRANITE.getDefaultState(), 0.3)
                .add(Blocks.DIORITE.getDefaultState(), 0.2)
                // Note - you can specify the noise type to be used for a layer using the Source enum (default is Perlin)
                .add(Source.RIDGE, Blocks.STONE.getDefaultState(), 0.1)
                .build();

        // Register to a specific biome. This creates a new geology system specific to that biome. The boolean
        // flag 'inheritGlobal' determines whether global strata layers should be copied to this new geology system.
        // If set false then only the strata specifically registered to it will be used.
        event.getManager().register(Biomes.OCEAN, strata, true);
    }

    @SubscribeEvent
    public static void features(SetupEvent.Features event) {
        log(event);
    }

    @SubscribeEvent
    public static void columns(SetupEvent.Decorators event) {
        log(event);
    }

    private static void log(SetupEvent<?> event) {
        Log.debug("Setting up {}", event.getManager().getClass().getSimpleName());
    }
}
