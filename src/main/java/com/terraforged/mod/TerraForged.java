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

import com.google.common.base.Suppliers;
import com.google.gson.GsonBuilder;
import com.mojang.serialization.JsonOps;
import com.terraforged.mod.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.util.function.Supplier;

public abstract class TerraForged implements Platform {
	public static final String MODID = "terraforged";
	public static final String TITLE = "TerraForged";
	public static final Logger LOG = LogManager.getLogger(TITLE);

	private final Supplier<Path> container;

	protected TerraForged(Supplier<Path> containerGetter) {
		this.container = Suppliers.memoize(containerGetter::get);
		Platform.ACTIVE_PLATFORM.set(this);
	}

	@Override
	public final Path getContainer() {
		return container.get();
	}

	public static Platform getPlatform() {
		return Platform.ACTIVE_PLATFORM.get();
	}

	public static ResourceLocation location(String name) {
		return new ResourceLocation(MODID, name);
	}

	public static <T> ResourceKey<Registry<T>> registry(String name) {
		return ResourceKey.createRegistryKey(location(name));
	}

	public static void dump() {
		var settings = BuiltinRegistries.NOISE_GENERATOR_SETTINGS.get(NoiseGeneratorSettings.OVERWORLD);

		SurfaceRules.RuleSource.CODEC.encodeStart(JsonOps.INSTANCE, settings.surfaceRule()).result().ifPresent(json -> {

			var gson = new GsonBuilder().setPrettyPrinting().create();

			System.err.println("\n\n" + gson.toJson(json) + "\n\n");

		});
	}
}
