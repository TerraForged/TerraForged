package com.terraforged.mod.chunk.column;

import com.terraforged.api.chunk.column.BlockColumn;
import com.terraforged.world.geology.DepthBuffer;

public class ColumnResource {

    private static final ThreadLocal<ColumnResource> LOCAL = ThreadLocal.withInitial(ColumnResource::new);

    public final DepthBuffer depthBuffer = new DepthBuffer();
    public final BlockColumn column = new BlockColumn();

    public static ColumnResource get() {
        return LOCAL.get();
    }
}
