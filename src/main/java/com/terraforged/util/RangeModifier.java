package com.terraforged.util;

public abstract class RangeModifier {

    protected final float from;
    protected final float to;
    protected final float max;
    private final float range;

    public RangeModifier(float from, float max, boolean exclusive) {
        this.from = from;
        this.to = max;
        this.max = exclusive ? 0 : 1;
        this.range = Math.abs(max - from);
    }

    public float apply(float value) {
        if (from < to) {
            if (value <= from) {
                return 0F;
            }
            if (value >= to) {
                return max;
            }
            return (value - from) / range;
        } else if (from > to) {
            if (value <= to) {
                return max;
            }
            if (value >= from) {
                return 0F;
            }
            return 1 - ((value - to) / range);
        }
        return 0F;
    }
}
