package com.terraforged.mod.util.function;

import java.io.Closeable;
import java.io.IOException;

public interface IOFunction<T, V extends Closeable> {

    V apply(T t) throws IOException;
}
