package com.terraforged.mod.platform;

import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicReference;

public interface Platform {
    AtomicReference<Platform> ACTIVE_PLATFORM = new AtomicReference<>();

    Path getContainer();
}
