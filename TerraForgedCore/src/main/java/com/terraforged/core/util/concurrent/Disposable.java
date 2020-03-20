package com.terraforged.core.util.concurrent;

public interface Disposable {

    void dispose();

    interface Listener<T> {

        void onDispose(T t);
    }
}
