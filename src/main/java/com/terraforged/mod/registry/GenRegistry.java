package com.terraforged.mod.registry;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonParseException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Lifecycle;
import com.terraforged.mod.TerraForged;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.RegistryReadOps;
import net.minecraft.resources.ResourceKey;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class GenRegistry {
    private static final GenRegistry INSTANCE = new GenRegistry();

    protected final List<Holder<?>> holders = new ArrayList<>();
    protected final AtomicBoolean init = new AtomicBoolean(false);

    public <T> void register(ResourceKey<Registry<T>> key, Codec<T> codec) {
        holders.add(new Holder<>(key, codec));
    }

    public Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> extend(
            Map<? extends ResourceKey<? extends Registry<?>>, ? extends MappedRegistry<?>> map) {

        if (holders.isEmpty()) return map;

        init();

        var builder = ImmutableMap.<ResourceKey<? extends Registry<?>>, MappedRegistry<?>>builder();
        builder.putAll(map);

        for (var holder : holders) {
            builder.put(holder.key, new MappedRegistry<>(holder.key, Lifecycle.stable()));
        }

        return builder.build();
    }

    public void load(RegistryAccess access, RegistryReadOps<?> ops) {
        TerraForged.LOG.info("Loading world-gen registry extensions:");
        for (var holder : holders) {
            load(holder, access, ops);
        }
    }

    protected <T> void load(Holder<T> holder, RegistryAccess access, RegistryReadOps<?> ops) {
        var registry = access.ownedRegistry(holder.key());
        if (registry.isEmpty()) return;

        var mappedRegistry = (MappedRegistry<T>) registry.get();
        if (mappedRegistry.size() != 0) return;

        var result = ops.decodeElements(mappedRegistry, holder.key(), holder.direct());
        TerraForged.LOG.info(" - World-gen registry extension: {} Size: {}", holder.key, mappedRegistry.size());

        result.error().ifPresent((partialResult) -> {
            throw new JsonParseException("Error loading registry data: " + partialResult.message());
        });
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    protected void init() {
        if (init.compareAndSet(false, true)) {
            TerraForged.LOG.info("Initializing builtin world-gen component registries");

            var writableRegistry = (WritableRegistry) BuiltinRegistries.REGISTRY;

            for (var holder : holders) {
                writableRegistry.register(holder.key, new MappedRegistry<>(holder.key, Lifecycle.stable()), Lifecycle.stable());
            }
        }
    }

    protected record Holder<T>(ResourceKey<Registry<T>> key, Codec<T> direct) {}

    public static GenRegistry get() {
        return INSTANCE;
    }
}
