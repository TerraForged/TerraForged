package com.terraforged.core.util.grid;

import me.dags.noise.util.NoiseUtil;

import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class FixedList<T> implements Iterable<T> {

    private final int maxIndex;
    private final T[] elements;

    FixedList(T[] elements) {
        this.maxIndex = elements.length - 1;
        this.elements = elements;
    }

    public T get(int index) {
        if (index < 0) {
            return elements[0];
        }
        if (index > maxIndex) {
            return elements[maxIndex];
        }
        return elements[index];
    }

    public T get(float value) {
        return get(indexOf(value));
    }

    public int size() {
        return elements.length;
    }

    public int indexOf(float value) {
        return NoiseUtil.round(value * maxIndex);
    }

    public Set<T> uniqueValues() {
        Set<T> set = new HashSet<>();
        Collections.addAll(set, elements);
        return set;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private int index = 0;

            @Override
            public boolean hasNext() {
                return index < elements.length;
            }

            @Override
            public T next() {
                return elements[index++];
            }
        };
    }

    @SuppressWarnings("unchecked")
    public static <T> FixedList<T> of(List<T> list) {
        return new FixedList<>((T[]) list.toArray());
    }
}
