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

package com.terraforged.mod.profiler.watchdog;

import com.terraforged.mod.profiler.Profiler;

public class UncheckedException extends RuntimeException {

    private static final String MESSAGE = "Critical error detected whilst generating %s %s";
    private static final String SEARCH_MESSAGE = "Critical error encountered whilst searching for structure: [%s]";

    public UncheckedException(String message, StackTraceElement[] stacktrace) {
        super(message);
        super.setStackTrace(stacktrace);
    }

    public UncheckedException(String phase, Object identity, Throwable cause) {
        this(MESSAGE, phase, identity, cause);
    }

    public UncheckedException(String message, String phase, Object identity, Throwable cause) {
        super(createMessage(message, phase, identity), cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    private static String createMessage(String format, String phase, Object identity) {
        String phaseName = nonNullStringValue(phase, "unknown");
        String identName = nonNullStringValue(identity, "unknown");
        return String.format(format, phaseName, identName);
    }

    protected static String nonNullStringValue(Object o, String def) {
        return o != null ? o.toString() : def;
    }

    public static UncheckedException search(Object identity, Throwable cause) {
        return new UncheckedException(SEARCH_MESSAGE, Profiler.STRUCTURE_SEARCHES.getReportDescription(), identity, cause);
    }
}
