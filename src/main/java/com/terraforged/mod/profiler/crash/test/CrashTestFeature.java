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

package com.terraforged.mod.profiler.crash.test;

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
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class CrashTestFeature extends Feature<CrashTestConfig> {

    public static final CrashTestFeature INSTANCE = new CrashTestFeature();
    private static final int CRASH_CHANCE_PERCENTAGE = 2;

    private CrashTestFeature() {
        super(CrashTestConfig.CODEC);
        setRegistryName(TerraForgedMod.MODID, "crashy_mccrashface");
    }

    @Override
    public boolean generate(ISeedReader region, ChunkGenerator generator, Random rand, BlockPos pos, CrashTestConfig config) {
        if (ThreadLocalRandom.current().nextInt(100) < CRASH_CHANCE_PERCENTAGE) {
            switch (config.crashType) {
                case DEADLOCK:
                    serverDeadlock(region, pos);
                    break;
                case EXCEPTION:
                    uncheckedException();
                    break;
                case SLOW:
                    generateSlowly(rand);
                    break;
            }
        }
        return true;
    }

    private void uncheckedException() {
        // Simulate some random unchecked exception being thrown during feature gen
        throw new NullPointerException("Crash time baby!");
    }

    private void generateSlowly(Random rand) {
        try {
            Thread.sleep(500 + rand.nextInt(500));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void serverDeadlock(ISeedReader region, BlockPos pos) {
        // Simulate a third-party mod requesting chunks via the ServerWorld rather than the
        // WorldGenRegion. This may be an indirect offense such as spawning an Entity but
        // then using unsafe methods on the Entity (ie setLocationAndRotation)
        int x = 50 + pos.getX() >> 4;
        int z = 124 + pos.getZ() >> 4;
        region.getWorld().getChunk(x, z);
    }

//    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
    public static class Init {
        @SubscribeEvent
        public static void registerFeatures(RegistryEvent.Register<Feature<?>> event) {
            Log.info("Registering crash-test feature");
            event.getRegistry().register(CrashTestFeature.INSTANCE);
        }
    }

//    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE)
    public static class Setup {
        @SubscribeEvent
        public static void setup(SetupEvent.Features event) {
            Log.info("Adding crash-test");
            event.getManager().getAppenders().add(BiomeFeatureMatcher.ANY, FeatureAppender.head(
                    GenerationStage.Decoration.VEGETAL_DECORATION,
                    CrashTestFeature.INSTANCE.withConfiguration(new CrashTestConfig(CrashTestConfig.CrashType.SLOW))
            ));

            event.getManager().getAppenders().add(BiomeFeatureMatcher.ANY, FeatureAppender.tail(
                    GenerationStage.Decoration.VEGETAL_DECORATION,
                    CrashTestFeature.INSTANCE.withConfiguration(new CrashTestConfig(CrashTestConfig.CrashType.DEADLOCK))
            ));
        }
    }
}
