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

package com.terraforged.mod.feature;

import com.google.common.collect.ImmutableMap;
import com.terraforged.mod.feature.sapling.SaplingConfig;
import com.terraforged.mod.featuremanager.data.DataManager;
import com.terraforged.mod.featuremanager.util.Json;
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
