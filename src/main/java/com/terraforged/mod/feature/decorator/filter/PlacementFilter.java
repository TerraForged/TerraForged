package com.terraforged.mod.feature.decorator.filter;

import com.terraforged.api.feature.decorator.DecorationContext;
import net.minecraft.util.math.BlockPos;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;

public class PlacementFilter {

    private static final Map<String, PlacementFilter> REGISTRY = new ConcurrentHashMap<>();

    static {
        register("biome", (ctx, pos) -> ctx.getBiome(pos) == ctx.getBiomes().getFeatureBiome());
    }

    private final String name;
    private final BiPredicate<DecorationContext, BlockPos> filter;

    public PlacementFilter(String name, BiPredicate<DecorationContext, BlockPos> filter) {
        this.name = name;
        this.filter = filter;
    }

    public String getName() {
        return name;
    }

    public boolean test(DecorationContext context, BlockPos pos) {
        return true; // filter.test(context, pos);
    }

    private static void register(String name, BiPredicate<DecorationContext, BlockPos> filter) {
        REGISTRY.put(name, new PlacementFilter(name, filter));
    }

    public static Optional<PlacementFilter> decode(String name) {
        return Optional.ofNullable(REGISTRY.get(name));
    }
}
