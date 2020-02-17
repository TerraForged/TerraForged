package com.terraforged.core.util.concurrent.batcher;

import java.util.concurrent.Callable;

public class SyncBatcher implements Batcher {

    @Override
    public void submit(Runnable task) {
        task.run();
    }

    @Override
    public void submit(Callable<?> task) {
        try {
            task.call();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void close() {

    }
}
