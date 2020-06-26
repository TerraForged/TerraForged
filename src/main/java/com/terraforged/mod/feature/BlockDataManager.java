package com.terraforged.mod.feature;

import com.google.common.collect.ImmutableMap;
import com.terraforged.mod.feature.sapling.SaplingConfig;
import com.terraforged.fm.data.DataManager;
import com.terraforged.fm.util.Json;
import net.minecraft.block.Block;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class BlockDataManager {

    private static final Map<String, BlockDataConfig.Parser> parsers = ImmutableMap.<String, BlockDataConfig.Parser>builder()
            .put("terraforged:sapling", SaplingConfig::deserialize)
            .build();

    private final Map<Block, BlockDataConfig> blockdata = new HashMap<>();

    public BlockDataManager(DataManager dataManager) {
        dataManager.forEachJson("blocks/saplings", (name, data) -> {
            String type = Json.getString("type", data.getAsJsonObject(), "");
            BlockDataConfig.Parser parser = parsers.get(type);
            if (parser == null) {
                return;
            }
            parser.parse(data.getAsJsonObject()).ifPresent(config -> blockdata.put(config.getTarget(), config));
        });
    }

    public <T extends BlockDataConfig> Optional<T> getConfig(Block block, Class<T> type) {
        return Optional.ofNullable(blockdata.get(block)).filter(type::isInstance).map(type::cast);
    }
}
