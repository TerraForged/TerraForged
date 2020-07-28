package com.terraforged.mod.util.function;

import java.io.IOException;

public interface IOSupplier<T> {

    T get() throws IOException;
}
