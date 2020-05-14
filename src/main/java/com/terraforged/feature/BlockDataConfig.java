package com.terraforged.feature;

import com.google.gson.JsonObject;
import net.minecraft.block.Block;

import java.util.Optional;

public interface BlockDataConfig {

    Block getTarget();

    interface Parser {

        Optional<? extends BlockDataConfig> parse(JsonObject data);
    }
}
