package com.terraforged.mod.platform.fabric;

import com.terraforged.mod.TerraForged;
import com.terraforged.mod.init.ModInit;
import com.terraforged.mod.platform.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.nio.file.Files;
import java.nio.file.Path;

public class TerraForgedMod extends TerraForged<ModContainer> implements Platform, ModInitializer {
    public TerraForgedMod() {
        super(FabricLoader.getInstance()::getModContainer);
    }

    @Override
    public void onInitialize() {
        ModInit.init();
    }

    @Override
    public Path getContainer() {
        return getModContainer().getRootPath();
    }

    @Override
    public Path getFile(Path path) {
        var container = getModContainer();
        var root = container.getRootPath();
        var file = container.getPath(path.toString());

        if (!Files.exists(file) && Files.isDirectory(root)) {
            file = getDevEnvPath(root, path);
        }

        return file;
    }

    protected static Path getDevEnvPath(Path root, Path file) {
        return root.getParent().resolve("resources").resolve(file);
    }
}
