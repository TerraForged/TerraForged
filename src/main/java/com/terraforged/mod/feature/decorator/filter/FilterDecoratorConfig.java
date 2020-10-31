package com.terraforged.mod.feature.decorator.filter;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.Dynamic;
import com.mojang.serialization.DynamicOps;
import com.terraforged.fm.util.codec.CodecException;
import com.terraforged.fm.util.codec.Codecs;
import net.minecraft.world.gen.placement.ConfiguredPlacement;
import net.minecraft.world.gen.placement.IPlacementConfig;

public class FilterDecoratorConfig implements IPlacementConfig {

    public static final Codec<FilterDecoratorConfig> CODEC = Codecs.create(
            FilterDecoratorConfig::encode,
            FilterDecoratorConfig::decode
    );

    public final ConfiguredPlacement<?> placement;
    public final PlacementFilter filter;

    public FilterDecoratorConfig(ConfiguredPlacement<?> placement, PlacementFilter filter) {
        this.placement = placement;
        this.filter = filter;
    }

    private static <T> Dynamic<T> encode(FilterDecoratorConfig config, DynamicOps<T> ops) {
        return new Dynamic<>(ops, ops.createMap(ImmutableMap.of(
                ops.createString("placement"), Codecs.encodeAndGet(ConfiguredPlacement.field_236952_a_, config.placement, ops),
                ops.createString("filter"), ops.createString(config.filter.getName())
        )));
    }

    private static <T> FilterDecoratorConfig decode(Dynamic<T> dynamic) {
        ConfiguredPlacement<?> placement = Codecs.decodeAndGet(ConfiguredPlacement.field_236952_a_, dynamic.get("placement"));
        PlacementFilter filter = Codecs.getResult(dynamic.get("filter").asString()).flatMap(PlacementFilter::decode)
                .orElseThrow(CodecException.get("Unknown filter name"));
        return new FilterDecoratorConfig(placement, filter);
    }
}
