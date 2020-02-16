package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;
import me.dags.noise.Module;

@Serializable
public class TerrainSettings {

    public Terrain steppe = new Terrain(5F, 1F, 1F);
    public Terrain plains = new Terrain(5F, 1F, 1F);
    public Terrain hills = new Terrain(2F, 1F, 1F);
    public Terrain dales = new Terrain(2F, 1F, 1F);
    public Terrain plateau = new Terrain(2F, 1F, 1F);
    public Terrain badlands = new Terrain(2F, 1F, 1F);
    public Terrain torridonian = new Terrain(0.5F, 1F, 1F);
    public Terrain mountains = new Terrain(0.5F, 1F, 1F);
    public Terrain volcano = new Terrain(1F, 1F, 1F);

    @Serializable
    public static class Terrain {

        @Range(min = 0, max = 10)
        @Comment("Controls how common this terrain type is")
        public float weight = 1F;

        @Range(min = 0, max = 2)
        @Comment("Controls the base height of this terrain")
        public float baseScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain vertically")
        public float verticalScale = 1F;

        @Range(min = 0F, max = 10F)
        @Comment("Stretches or compresses the terrain horizontally")
        public float horizontalScale = 1F;

        public Terrain() {

        }

        public Terrain(float weight, float vertical, float horizontal) {
            this.weight = weight;
            this.verticalScale = vertical;
            this.horizontalScale = horizontal;
        }

        public Module apply(double bias, double scale, Module module) {
            double moduleBias = bias * baseScale;
            double moduleScale = scale * verticalScale;
            if (moduleBias + moduleScale > 1) {
                return module.scale(moduleScale).bias(moduleBias).clamp(0, 1);
            }
            return module.scale(moduleScale).bias(moduleBias);
        }
    }
}
