package com.terraforged.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.terraforged.Log;

import java.util.function.Supplier;

public class ConfigRef implements Supplier<CommentedFileConfig> {

    private final Object lock = new Object();
    private final Supplier<CommentedFileConfig> factory;

    private CommentedFileConfig ref;

    public ConfigRef(Supplier<CommentedFileConfig> factory) {
        this.factory = factory;
    }

    @Override
    public CommentedFileConfig get() {
        synchronized (lock) {
            if (ref != null) {
                Log.info("Loading config: %s", ref.getFile().getName());
                ref.load();
                return ref;
            }
            return ref = factory.get();
        }
    }
}
