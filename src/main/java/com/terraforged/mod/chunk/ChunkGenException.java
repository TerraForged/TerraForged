package com.terraforged.mod.chunk;

import com.terraforged.mod.util.Environment;

public class ChunkGenException extends RuntimeException {

    public ChunkGenException(Throwable cause) {
        super(cause);
    }

    public static void handle(Throwable t) throws ChunkGenException {
        if (Environment.isVerbose()) {
            // ChunkGen exceptions get lost in CompletableFuture land so print them here
            // so we can find out what went wrong.
            t.printStackTrace();
        }
        throw new ChunkGenException(t);
    }
}
