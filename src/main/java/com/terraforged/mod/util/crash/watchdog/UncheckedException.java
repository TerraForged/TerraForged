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

package com.terraforged.mod.util.crash.watchdog;

public class UncheckedException extends RuntimeException {

    protected static final String UNCHECKED = "Critical error detected whilst generating ";
    protected static final String DEADLOCK = "Server deadlock detected whilst generating ";

    public UncheckedException(String prefix, String phase, Object identity, StackTraceElement[] stacktrace) {
        super(createMessage(prefix, phase, identity));
        super.setStackTrace(stacktrace);
    }

    public UncheckedException(String phase, Object identity, Throwable cause) {
        super(createMessage(UNCHECKED, phase, identity), cause);
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }

    protected static String createMessage(String prefix, String phase, Object identity) {
        String phaseName = nonNullStringValue(phase, "unknown");
        String identName = nonNullStringValue(identity, "unknown");
        return prefix + phaseName + " " + identName;
    }

    private static String nonNullStringValue(Object o, String def) {
        return o != null ? o.toString() : def;
    }
}
