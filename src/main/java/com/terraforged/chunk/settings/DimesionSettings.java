package com.terraforged.chunk.settings;

import com.terraforged.TerraWorld;
import com.terraforged.core.serialization.annotation.Comment;
import com.terraforged.core.serialization.annotation.Range;
import com.terraforged.core.serialization.annotation.Serializable;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.storage.WorldInfo;
import org.jline.utils.Log;

@Serializable
public class DimesionSettings {

    public BaseDecorator bedrockLayer = new BaseDecorator();

    public DimensionGenerators dimensions = new DimensionGenerators();

    @Serializable
    public static class BaseDecorator {

        @Comment("Controls the material that should be used in the world's base layer")
        public String material = "minecraft:bedrock";

        @Range(min = 0, max = 10)
        @Comment("Controls the minimum height of the world's base layer")
        public int minDepth = 1;

        @Range(min = 0, max = 10)
        @Comment("Controls the amount of height randomness of the world's base layer")
        public int variance = 4;
    }

    @Serializable
    public static class DimensionGenerators {

        @Comment("Select the nether generator")
        public String nether = "default";

        @Comment("Select the end generator")
        public String end = "default";

        public void apply(WorldInfo info) {
            set(info, DimensionType.THE_NETHER, nether);
            set(info, DimensionType.THE_END, end);
        }
    }

    public static WorldType getWorldType(WorldInfo info, DimensionType type) {
        String generator = info.getDimensionData(type).getString("TerraDelegateGenerator");
        return getWorldType(generator);
    }

    private static void set(WorldInfo info, DimensionType type, String value) {
        CompoundNBT data = info.getDimensionData(type);
        data.put("TerraDelegateGenerator", StringNBT.valueOf(getWorldType(value).getName()));
        info.setDimensionData(type, data);
    }

    private static WorldType getWorldType(String name) {
        WorldType type = WorldType.byName(name);
        if (type == null) {
            Log.warn("WorldType {} does not exist. Reverting to default", name);
            return WorldType.DEFAULT;
        }
        if (TerraWorld.isTerraType(type)) {
            Log.warn("Cannot set TerraForged as world type for {}", name);
            return WorldType.DEFAULT;
        }
        return type;
    }
}
