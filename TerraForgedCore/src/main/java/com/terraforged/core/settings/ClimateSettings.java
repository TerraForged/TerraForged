package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;
import me.dags.noise.Module;
import me.dags.noise.Source;

@Serializable
public class ClimateSettings {

    public RangeValue moisture = new RangeValue(0, 1F);

    public RangeValue temperature = new RangeValue(0, 1F);

    @Serializable
    public static class RangeValue {

        @Range(min = 0F, max = 1F)
        @Comment("The lower limit of the range")
        public float min;

        @Range(min = 0F, max = 1F)
        @Comment("The upper limit of the range")
        public float max;

        public RangeValue() {
            this(0, 1);
        }

        public RangeValue(float min, float max) {
            this.min = min;
            this.max = max;
        }

        public float getMin() {
            return Math.min(min, max);
        }

        public float getMax() {
            return Math.max(min, max);
        }

        public Module clamp(Module module) {
            float min = getMin();
            float max = getMax();
            float range = max - min;
            if (range == 0) {
                return Source.constant(min);
            }
            return module.scale(range).bias(min);
        }
    }
}
