package com.terraforged.core.settings;

import com.terraforged.core.util.serialization.annotation.Comment;
import com.terraforged.core.util.serialization.annotation.Range;
import com.terraforged.core.util.serialization.annotation.Serializable;
import me.dags.noise.Module;
import me.dags.noise.Source;

@Serializable
public class GeneratorSettings {

    public transient long seed = 0L;

    /**
     * WORLD PROPERTIES
     */
    public World world = new World();

    /**
     * TERRAIN PROPERTIES
     */
    public Land land = new Land();

    /**
     * BIOME PROPERTIES
     */
    public Biome biome = new Biome();

    public BiomeNoise biomeEdgeNoise = new BiomeNoise();

    @Serializable
    public static class World {

        @Range(min = 0, max = 256)
        @Comment("Controls the world height")
        public int worldHeight = 256;

        @Range(min = 0, max = 255)
        @Comment("Controls the sea level")
        public int seaLevel = 63;

        @Range(min = 0F, max = 1F)
        @Comment("Controls the amount of ocean between continents")
        public float oceanSize = 0.25F;
    }

    @Serializable
    public static class Land {

        @Range(min = 500, max = 10000)
        @Comment("Controls the size of continents")
        public int continentScale = 4000;

        @Range(min = 250, max = 5000)
        @Comment("Controls the size of mountain ranges")
        public int mountainScale = 950;

        @Range(min = 125, max = 2500)
        @Comment("Controls the size of terrain regions")
        public int regionSize = 1000;
    }

    @Serializable
    public static class Biome {

        @Range(min = 50, max = 500)
        @Comment("Controls the size of individual biomes")
        public int biomeSize = 200;

        @Range(min = 1, max = 200)
        @Comment("Controls the scale of shape distortion for biomes")
        public int biomeWarpScale = 35;

        @Range(min = 1, max = 200)
        @Comment("Controls the strength of shape distortion for biomes")
        public int biomeWarpStrength = 70;
    }

    @Serializable
    public static class BiomeNoise {

        @Comment("The noise type")
        public Source type = Source.PERLIN;

        @Range(min = 1, max = 100)
        @Comment("Controls the scale of the noise")
        public int scale = 8;

        @Range(min = 1, max = 5)
        @Comment("Controls the number of noise octaves")
        public int octaves = 2;

        @Range(min = 0F, max = 5.5F)
        @Comment("Controls the gain subsequent noise octaves")
        public float gain = 0.5F;

        @Range(min = 0F, max = 10.5F)
        @Comment("Controls the lacunarity of subsequent noise octaves")
        public float lacunarity = 2.5F;

        @Range(min = 1, max = 100)
        @Comment("Controls the strength of the noise")
        public int strength = 24;

        public Module build(int seed) {
            return Source.build(seed, scale, octaves).gain(gain).lacunarity(lacunarity).build(type).bias(-0.5);
        }
    }
}
