package com.terraforged.fm.template.template;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;

public class BakedDimensions extends BakedTransform<Dimensions> {

    public BakedDimensions(Dimensions value) {
        super(Dimensions[]::new, value);
    }

    @Override
    protected Dimensions apply(Mirror mirror, Rotation rotation, Dimensions value) {
        Vector3i min = Template.transform(value.min, mirror, rotation);
        Vector3i max = Template.transform(value.max, mirror, rotation);
        return compile(min, max);
    }

    public static Dimensions compile(Vector3i min, Vector3i max) {
        int minX = Math.min(min.getX(), max.getX());
        int minY = Math.min(min.getY(), max.getY());
        int minZ = Math.min(min.getZ(), max.getZ());
        int maxX = Math.max(min.getX(), max.getX());
        int maxY = Math.max(min.getY(), max.getY());
        int maxZ = Math.max(min.getZ(), max.getZ());
        return new Dimensions(new BlockPos(minX, minY, minZ), new BlockPos(maxX, maxY, maxZ));
    }
}
