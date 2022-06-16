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

import com.terraforged.mod.registry.key.RegistryKey;
import com.terraforged.mod.worldgen.asset.*;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.biome.Biome;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class TerraForged {
	public static final String MODID = "terraforged";
	public static final String TITLE = "TerraForged";
	public static final String DATAPACK_VERSION = "v0.1";
	public static final Logger LOG = LogManager.getLogger(TITLE);

	public static final ResourceLocation WORLD_PRESET = location("normal");
	public static final ResourceLocation DIMENSION_EFFECTS = location("overworld");

	public static final RegistryKey<Biome> BIOMES = RegistryKey.builtin(() -> Registry.BIOME_REGISTRY);

	// Note: Forge fucks around with mod datapack registry paths in a way that would make these
	// keys break on other platforms, so we must register them under the minecraft namespace :/
	public static final RegistryKey<ClimateType> CLIMATES = registry("minecraft:worldgen/climate");
	public static final RegistryKey<NoiseCave> CAVES = registry("minecraft:worldgen/cave");
	public static final RegistryKey<TerrainNoise> TERRAINS = registry("minecraft:worldgen/terrain/noise");
	public static final RegistryKey<TerrainType> TERRAIN_TYPES = registry("minecraft:worldgen/terrain/type");
	public static final RegistryKey<VegetationConfig> VEGETATIONS = registry("minecraft:worldgen/vegetation");

	protected TerraForged() {
		Environment.log();
	}

	public static ResourceLocation location(String name) {
		if (name.contains(":")) return new ResourceLocation(name);

		return new ResourceLocation(MODID, name);
	}

	public static <T> RegistryKey<T> registry(String name) {
		return new RegistryKey<>(location(name));
	}
}
