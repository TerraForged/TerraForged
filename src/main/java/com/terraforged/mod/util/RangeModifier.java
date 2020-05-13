package com.terraforged.mod.util;

public abstract class RangeModifier {

    protected final float min;
    protected final float max;
    private final float range;

    public RangeModifier(float min, float max) {
        this.min = min;
        this.max = max;
        this.range = Math.abs(max - min);
    }

    public float apply(float value) {
        if (min < max) {
            if (value <= min) {
                return 0F;
            }
            if (value >= max) {
                return 1F;
            }
            return (value - min) / range;
        } else if (min > max) {
            if (value <= min) {
                return 1F;
            }
            if (value >= max) {
                return 0F;
            }
            return 1F - (value / range);
        }
        return 1F;
    }
}
