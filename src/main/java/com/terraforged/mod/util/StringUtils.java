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

package com.terraforged.mod.util;

import com.terraforged.core.concurrent.Resource;
import com.terraforged.core.concurrent.pool.ObjectPool;

public class StringUtils {

    private static final ObjectPool<Builder> STRING_BUILDERS = new ObjectPool<>(10, Builder::new);

    public static Resource<StringBuilder> retain() {
        Resource<Builder> resource = STRING_BUILDERS.get();
        return resource.get().setSelf(resource);
    }

    public static String repeat(char c, int times) {
        try (Resource<StringBuilder> resource = retain()) {
            StringBuilder sb = resource.get();
            while (times-- > 0) {
                sb.append(c);
            }
            return sb.toString();
        }
    }

    public static StringBuilder padAppend(StringBuilder sb, String prefix, String text, int width) {
        sb.append(prefix);
        return padAppend(sb, text, width);
    }

    public static StringBuilder padAppend(StringBuilder sb, String text, int width) {
        for (int pad = width - text.length(); pad > 0; pad--) {
            sb.append(' ');
        }
        return sb.append(text);
    }

    private static class Builder implements Resource<StringBuilder> {

        private final StringBuilder sb = new StringBuilder();
        private Resource<Builder> self;

        private Builder setSelf(Resource<Builder> self) {
            this.self = self;
            return this;
        }

        @Override
        public StringBuilder get() {
            return sb;
        }

        @Override
        public boolean isOpen() {
            return self != null && self.isOpen();
        }

        @Override
        public void close() {
            Resource<Builder> resource = this.self;
            // required to prevent overflowing
            this.self = null;

            if (resource != null) {
                sb.setLength(0);
                resource.close();
            }
        }
    }
}
