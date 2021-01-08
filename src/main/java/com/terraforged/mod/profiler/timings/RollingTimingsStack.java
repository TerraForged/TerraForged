/*
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
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

package com.terraforged.mod.profiler.timings;

// Fixed size queue of entries which overwrites the oldest value when at capacity
public class RollingTimingsStack implements TimingStack {

    private final Entry[] entries = new Entry[16];

    private int index = -1;
    private int size = 0;

    public RollingTimingsStack() {
        for (int i = 0; i < entries.length; i++) {
            entries[i] = new Entry();
        }
    }

    private RollingTimingsStack(RollingTimingsStack other) {
        this.index = other.index;
        this.size = other.size;
        for (int i = 0; i < entries.length; i++) {
            entries[i] = other.entries[i].copy();
        }
    }

    @Override
    public int size() {
        return size;
    }

    @Override
    public RollingTimingsStack copy() {
        return new RollingTimingsStack(this);
    }

    @Override
    public RollingTimingsStack reset() {
        index = -1;
        size = 0;
        for (Entry entry : entries) {
            entry.reset();
        }
        return this;
    }

    @Override
    public void push(String type, Object identifier, long time) {
        entries[nextIndex()].set(type, identifier, time);
        size = Math.min(entries.length, size + 1);
    }

    @Override
    public <T> void iterate(T ctx, TimingStack.Visitor<T> consumer) {
        int offset = (1 + index - size) & 15;
        for (int i = size - 1; i >= 0; i--) {
            int index = (offset + i) & 15;
            Entry entry = entries[index];
            if (entry.empty()) {
                continue;
            }
            consumer.visit(entry.type, entry.identifier, entry.time, ctx);
        }
    }

    private int nextIndex() {
        index = (index + 1) & 15;
        return index;
    }

    private static class Entry {

        private String type = null;
        private Object identifier = null;
        private long time = 0L;

        private boolean empty() {
            return type == null && identifier == null;
        }

        private void reset() {
            type = null;
            identifier = null;
            time = 0L;
        }

        private void set(String type, Object identifier, long time) {
            this.identifier = identifier;
            this.type = type;
            this.time = time;
        }

        private Entry copy() {
            Entry entry = new Entry();
            entry.set(type, identifier, time);
            return entry;
        }
    }
}
