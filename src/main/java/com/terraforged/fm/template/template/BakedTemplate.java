package com.terraforged.fm.template.template;

import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;

import java.util.stream.Stream;

public class BakedTemplate extends BakedTransform<BlockInfo[]> {

    public BakedTemplate(BlockInfo[] value) {
        super(BlockInfo[][]::new, value);
    }

    @Override
    protected BlockInfo[] apply(Mirror mirror, Rotation rotation, BlockInfo[] value) {
        return Stream.of(value).map(block -> block.transform(mirror, rotation)).toArray(BlockInfo[]::new);
    }
}
