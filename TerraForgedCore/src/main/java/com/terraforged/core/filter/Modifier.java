package com.terraforged.core.filter;

import com.terraforged.core.cell.Cell;

public interface Modifier {

    float getModifier(float value);

    default float modify(Cell cell, float value) {
        return value * getModifier(cell.value);
    }

    default Modifier invert() {
        return v -> 1 - getModifier(v);
    }

    static Modifier range(float minValue, float maxValue) {
        return new Modifier() {

            private final float min = minValue;
            private final float max = maxValue;
            private final float range = maxValue - minValue;

            @Override
            public float getModifier(float value) {
                if (value > max) {
                    return 1F;
                }
                if (value < min) {
                    return 0F;
                }
                return (value - min) / range;
            }
        };
    }
}
