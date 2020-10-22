package com.terraforged.mod.util.reflect;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodAccessor<O> extends Accessor<O, Method> {

    private final Method method;

    MethodAccessor(Method method) {
        super(method);
        this.method = method;
    }

    public void invokeVoid(O owner, Object... input) throws IllegalAccessException, InvocationTargetException {
        method.invoke(owner, input);
    }

    public <V> V invoke(O owner, Class<V> resultType, Object... input) throws InvocationTargetException, IllegalAccessException {
        Object result = access.invoke(owner, input);
        if (resultType.isInstance(result)) {
            return resultType.cast(result);
        }
        return null;
    }
}
