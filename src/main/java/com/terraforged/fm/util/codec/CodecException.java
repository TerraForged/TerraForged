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

package com.terraforged.fm.util.codec;

import com.google.common.base.Suppliers;

import java.util.function.Supplier;

public class CodecException extends RuntimeException {

    public static final Supplier<CodecException> SUPPLIER = CodecException::new;

    private CodecException() {

    }

    private CodecException(String message) {
        super(message);
    }

    private CodecException(String message, Throwable cause) {
        super(message, cause);
    }

    public static CodecException of(String message, Object... args) {
        if (message == null) {
            return new CodecException();
        }
        return new CodecException(String.format(message, args));
    }

    public static CodecException of(Throwable cause, String message, Object... args) {
        if (message == null) {
            return new CodecException(":[", cause);
        }
        return new CodecException(String.format(message, args), cause);
    }

    public static Supplier<CodecException> get(String message, Object... args) {
        return () -> of(message, args);
    }

    public static Supplier<CodecException> get(String message, Throwable cause, Object... args) {
        return () -> of(message, cause, args);
    }
}
