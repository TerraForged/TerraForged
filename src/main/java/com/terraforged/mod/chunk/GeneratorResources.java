package com.terraforged.mod.chunk;

import com.terraforged.api.chunk.column.BlockColumn;
import com.terraforged.world.geology.DepthBuffer;

public class GeneratorResources {

    private static final ThreadLocal<GeneratorResources> LOCAL = ThreadLocal.withInitial(GeneratorResources::new);

    public final DepthBuffer depthBuffer = new DepthBuffer();
    public final BlockColumn column = new BlockColumn();

    public static GeneratorResources get() {
        return LOCAL.get();
    }
}
