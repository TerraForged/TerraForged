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

package com.terraforged.mod.mixin.benchmark;

import com.terraforged.mod.chunk.profiler.Profiler;
import com.terraforged.mod.chunk.profiler.Section;
import net.minecraft.util.registry.DynamicRegistries;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.ISeedReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeManager;
import net.minecraft.world.biome.provider.BiomeProvider;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.NoiseChunkGenerator;
import net.minecraft.world.gen.WorldGenRegion;
import net.minecraft.world.gen.feature.structure.StructureManager;
import net.minecraft.world.gen.feature.template.TemplateManager;
import net.minecraft.world.gen.settings.DimensionStructuresSettings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NoiseChunkGenerator.class)
public abstract class MixinNoiseChunkGenerator extends ChunkGenerator {

    public MixinNoiseChunkGenerator(BiomeProvider biomeProvider, DimensionStructuresSettings settings) {
        super(biomeProvider, settings);
    }

    @Inject(method = "func_230352_b_", at = @At("HEAD"))
    public void terrainIn(IWorld world, StructureManager structures, IChunk chunk, CallbackInfo ci) {
        Profiler.TERRAIN.punchIn();
    }

    @Inject(method = "func_230352_b_", at = @At("TAIL"))
    public void terrainOut(IWorld world, StructureManager structures, IChunk chunk, CallbackInfo ci) {
        Profiler.TERRAIN.punchOut();
    }

    @Inject(method = "generateSurface", at = @At("HEAD"))
    public void surfaceIn(WorldGenRegion region, IChunk chunk, CallbackInfo ci) {
        Profiler.SURFACE.punchIn();
    }

    @Inject(method = "generateSurface", at = @At("TAIL"))
    public void surfaceOut(WorldGenRegion region, IChunk chunk, CallbackInfo ci) {
        Profiler.SURFACE.punchOut();
    }

    @Inject(method = "func_230354_a_", at = @At("HEAD"))
    public void mobsIn(WorldGenRegion region, CallbackInfo ci) {
        Profiler.MOB_SPAWNS.punchIn();
    }

    @Inject(method = "func_230354_a_", at = @At("TAIL"))
    public void mobsOut(WorldGenRegion region, CallbackInfo ci) {
        Profiler.MOB_SPAWNS.punchOut();
    }

    @Override
    public void func_242707_a(DynamicRegistries registries, StructureManager structures, IChunk chunk, TemplateManager templates, long seed) {
        try (Section section = Profiler.STRUCTURE_STARTS.punchIn()) {
            super.func_242707_a(registries, structures, chunk, templates, seed);
        }
    }

    @Override
    public void func_235953_a_(ISeedReader world, StructureManager structures, IChunk chunk) {
        try (Section section = Profiler.STRUCTURE_REFS.punchIn()) {
            super.func_235953_a_(world, structures, chunk);
        }
    }

    @Override
    public void func_242706_a(Registry<Biome> registry, IChunk chunk) {
        try (Section section = Profiler.BIOMES.punchIn()) {
            super.func_242706_a(registry, chunk);
        }
    }

    @Override
    public void func_230350_a_(long seed, BiomeManager biomes, IChunk chunk, GenerationStage.Carving carver) {
        try (Section section = Profiler.CARVING.punchIn()) {
            super.func_230350_a_(seed, biomes, chunk, carver);
        }
    }

    @Override
    public void func_230351_a_(WorldGenRegion region, StructureManager structures) {
        try (Section section = Profiler.DECORATION.punchIn()) {
            super.func_230351_a_(region, structures);
        }
    }
}
