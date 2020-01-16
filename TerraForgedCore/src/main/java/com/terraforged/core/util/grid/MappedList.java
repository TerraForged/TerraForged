package com.terraforged.core.util.grid;

import java.util.List;

public class MappedList<T> extends FixedList<T> {

    private final float min;
    private final float range;

    public MappedList(T[] elements, float min, float range) {
        super(elements);
        this.min = min;
        this.range = range;
    }

    @Override
    public int indexOf(float value) {
        return super.indexOf((value - min) / range);
    }

    @SuppressWarnings("unchecked")
    public static <T> MappedList<T> of(List<T> list, float min, float range) {
        return new MappedList<>((T[]) list.toArray(), min, range);
    }
}
