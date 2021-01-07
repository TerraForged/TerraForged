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

package com.terraforged.mod.util.crash.watchdog.timings;

public class Top3TimingStack implements TimingStack {

    private final Entry entry0 = new Entry();
    private final Entry entry1 = new Entry();
    private final Entry entry2 = new Entry();

    @Override
    public TimingStack copy() {
        Top3TimingStack stack = new Top3TimingStack();
        stack.entry0.copy(entry0);
        stack.entry1.copy(entry1);
        stack.entry2.copy(entry2);
        return stack;
    }

    @Override
    public TimingStack reset() {
        entry0.reset();
        entry1.reset();
        entry2.reset();
        return null;
    }

    @Override
    public int getSize() {
        if (entry0.identifier == null) {
            return 0;
        }
        if (entry1.identifier == null) {
            return 1;
        }
        if (entry2.identifier == null) {
            return 2;
        }
        return 3;
    }

    @Override
    public void push(String type, Object identifier, long time) {
        if (entry0.set(type, identifier, time)) {
            return;
        }
        if (entry1.set(type, identifier, time)) {
            return;
        }
        entry2.set(type, identifier, time);
    }

    @Override
    public <T> void iterate(T ctx, Visitor<T> visitor) {
        visit(entry0, ctx, visitor);
        visit(entry1, ctx, visitor);
        visit(entry2, ctx, visitor);
    }

    private static <T> void visit(Entry entry, T ctx, Visitor<T> visitor) {
        if (entry.identifier != null) {
            visitor.visit(entry.type, entry.identifier, entry.time, ctx);
        }
    }

    private static class Entry {

        private String type = null;
        private Object identifier = null;
        private long time = 0L;

        private void reset() {
            type = null;
            identifier = null;
            time = 0L;
        }

        private void copy(Entry other) {
            type = other.type;
            identifier = other.identifier;
            time = other.time;
        }

        private boolean set(String type, Object identifier, long time) {
            if (identifier == null || time > this.time) {
                this.identifier = identifier;
                this.type = type;
                this.time = time;
                return true;
            }
            return false;
        }
    }
}
