package com.terraforged.core.util.concurrent.batcher;

import java.util.concurrent.Callable;

public interface Batcher extends AutoCloseable {

    void submit(Runnable task);

    void submit(Callable<?> task);

    @Override
    void close();
}
