package com.terraforged.core.region;

public class Size {

    public final int size;
    public final int total;
    public final int border;
    private final int mask;

    public Size(int size, int border) {
        this.size = size;
        this.mask = size - 1;
        this.border = border;
        this.total = size + (2 * border);
    }

    public int mask(int i) {
        return i & mask;
    }

    public int indexOf(int x, int z) {
        return (z * total) + x;
    }

    public static int chunkToBlock(int i) {
        return i << 4;
    }

    public static int blockToChunk(int i) {
        return i >> 4;
    }

    public static int count(int minX, int minZ, int maxX, int maxZ) {
        int dx = maxX - minX;
        int dz = maxZ - minZ;
        return dx * dz;
    }

    public static Size chunks(int factor, int borderChunks) {
        int chunks = 1 << factor;
        return new Size(chunks, borderChunks);
    }

    public static Size blocks(int factor, int borderChunks) {
        int chunks = 1 << factor;
        int blocks = chunks << 4;
        int borderBlocks = borderChunks << 4;
        return new Size(blocks, borderBlocks);
    }
}
