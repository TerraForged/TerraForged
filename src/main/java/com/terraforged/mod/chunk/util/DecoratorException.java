package com.terraforged.mod.chunk.util;

public class DecoratorException extends Exception {

    public DecoratorException(String type, String identity, Throwable t) {
        super(type + "@" + identity, t);
    }
}
