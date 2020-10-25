package com.terraforged.mod.chunk.profiler;

public interface Section extends AutoCloseable {

    Section punchIn();

    @Override
    void close();
}
