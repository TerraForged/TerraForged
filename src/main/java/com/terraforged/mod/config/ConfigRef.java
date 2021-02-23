/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.mod.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;

import java.util.concurrent.locks.StampedLock;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ConfigRef implements Supplier<CommentedFileConfig> {

    private final String configVersion;
    private final StampedLock lock = new StampedLock();
    private final Function<String, CommentedFileConfig> factory;

    private CommentedFileConfig ref;

    public ConfigRef(Supplier<CommentedFileConfig> factory) {
        this("", v -> factory.get());
    }

    public ConfigRef(String configVersion, Function<String, CommentedFileConfig> factory) {
        this.factory = factory;
        this.configVersion = configVersion;
    }

    @Override
    public CommentedFileConfig get() {
        long read = lock.readLock();
        try {
            if (ref != null) {
                ref.load();
                return ref;
            }
        } finally {
            lock.unlockRead(read);
        }

        long write = lock.writeLock();
        try {
            if (ref != null) {
                return ref;
            }
            return ref = factory.apply(configVersion);
        } finally {
            lock.unlockWrite(write);
        }
    }

    public ConfigRef load() {
        get();
        return this;
    }

    public ConfigRef save() {
        get().save();
        return this;
    }

    public void set(Consumer<CommentedFileConfig> setter) {
        CommentedFileConfig config = get();
        setter.accept(config);
        config.save();
    }

    public int getInt(String name, int def) {
        return getNumber(name, def).intValue();
    }

    public long getLong(String name, long def) {
        return getNumber(name, def).longValue();
    }

    public float getFloat(String name, float def) {
        return getNumber(name, def).floatValue();
    }

    public double getDouble(String name, double def) {
        return getNumber(name, def).doubleValue();
    }

    public boolean getBool(String name, boolean def) {
        return getValue(name, def);
    }

    public <T extends Number> Number getNumber(String name, T def) {
        return getValue(name, def);
    }

    public <T> T getValue(String name, T def) {
        long read = lock.tryReadLock();
        if (read != 0L) {
            try {
                CommentedFileConfig current = ref;
                if (current == null) {
                    return def;
                }
                return current.getOrElse(name, def);
            } catch (Throwable t) {
                return def;
            } finally {
                lock.unlockRead(read);
            }
        }
        return def;
    }

    public void forEach(BiConsumer<String, Object> consumer) {
        long read = lock.tryReadLock();
        if (read != 0L) {
            try {
                CommentedFileConfig current = ref;
                if (current != null) {
                    current.entrySet().forEach(entry -> consumer.accept(entry.getKey(), entry.getValue()));
                }
            } finally {
                lock.unlockRead(read);
            }
        }
    }
}
