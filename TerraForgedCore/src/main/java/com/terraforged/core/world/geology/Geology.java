package com.terraforged.core.world.geology;

import me.dags.noise.Module;

import java.util.ArrayList;
import java.util.List;

public class Geology<T> {

    private final Module selector;
    private final List<Strata<T>> backing = new ArrayList<>();

    public Geology(Module selector) {
        this.selector = selector;
    }

    public Geology<T> add(Geology<T> geology) {
        backing.addAll(geology.backing);
        return this;
    }

    public Geology<T> add(Strata<T> strata) {
        backing.add(strata);
        return this;
    }

    public Strata<T> getStrata(float x, int y) {
        float noise = selector.getValue(x, y);
        return getStrata(noise);
    }

    public Strata<T> getStrata(float value) {
        int index = (int) (value * backing.size());
        index = Math.min(backing.size() - 1, index);
        return backing.get(index);
    }
}
