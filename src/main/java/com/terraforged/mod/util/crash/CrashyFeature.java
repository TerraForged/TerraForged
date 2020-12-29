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

package com.terraforged.mod.util.crash;

import com.terraforged.mod.Log;
import com.terraforged.mod.TerraForgedMod;
import com.terraforged.mod.api.event.SetupEvent;
import com.terraforged.mod.featuremanager.matcher.BiomeFeatureMatcher;
import com.terraforged.mod.featuremanager.transformer.FeatureAppender;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CrashyFeature extends Feature<NoFeatureConfig> {

    public static final CrashyFeature INSTANCE = new CrashyFeature();
    private static final int CRASH_CHANCE_PERCENTAGE = 5;

    private CrashyFeature() {
        super(NoFeatureConfig.field_236558_a_);
        setRegistryName(TerraForgedMod.MODID, "crashy_mccrashface");
    }

    @Override
    public boolean generate(ISeedReader reader, ChunkGenerator generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) < CRASH_CHANCE_PERCENTAGE) {
            throw new NullPointerException("Crash time baby!");
        }
        return true;
    }

//    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Init {
        @SubscribeEvent
        public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
            Log.info("Registering crash-test feature");
            event.getRegistry().register(CrashyFeature.INSTANCE);
        }
    }

    //@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Setup {
        @SubscribeEvent
        public static void setup(SetupEvent.Features event) {
            Log.info("Adding crash-test");
            event.getManager().getAppenders().add(BiomeFeatureMatcher.ANY, FeatureAppender.tail(
                    GenerationStage.Decoration.VEGETAL_DECORATION,
                    CrashyFeature.INSTANCE.withConfiguration(NoFeatureConfig.NO_FEATURE_CONFIG)
            ));
        }
    }
}
