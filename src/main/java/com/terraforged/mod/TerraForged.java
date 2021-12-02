package com.terraforged.mod;

import com.google.common.base.Suppliers;
import com.terraforged.mod.platform.Platform;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
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
}
