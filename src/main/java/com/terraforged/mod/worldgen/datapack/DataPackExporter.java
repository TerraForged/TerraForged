package com.terraforged.mod.worldgen.datapack;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.util.FileUtil;
import net.minecraft.world.level.DataPackConfig;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class DataPackExporter {
    private static final String PACK = "file/" + TerraForged.TITLE;

    public static DataPackConfig setup(@Nullable Path dir, DataPackConfig config) {
        if (dir == null) throw new NullPointerException("Dir is null!");

        TerraForged.LOG.info("Generating TerraForged datapack");

        try {
            var dest = dir.resolve(TerraForged.TITLE + ".zip");
            var root = TerraForged.getPlatform().getContainer();
            FileUtil.createZipCopy(root, "default", dest);
        } catch (IOException e) {
            e.printStackTrace();
        }

        List<String> enabled = new ArrayList<>(config.getEnabled());
        enabled.add(PACK);

        List<String> disabled = new ArrayList<>(config.getDisabled());
        disabled.remove(PACK);

        return new DataPackConfig(enabled, disabled);
    }
}
