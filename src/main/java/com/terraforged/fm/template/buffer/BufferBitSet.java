package com.terraforged.fm.template.buffer;

import java.util.BitSet;

public class BufferBitSet {

    private int minX;
    private int minY;
    private int minZ;
    private int sizeX;
    private int sizeY;
    private int sizeZ;
    private int sizeXZ;
    private BitSet bitSet;

    public void assign(int x1, int y1, int z1, int x2, int y2, int z2) {
        this.minX = Math.min(x1, x2);
        this.minY = Math.min(y1, y2);
        this.minZ = Math.min(z1, z2);
        this.sizeX = Math.max(x1, x2) - minX;
        this.sizeY = Math.max(y1, y2) - minY;
        this.sizeZ = Math.max(z1, z2) - minZ;
        this.sizeXZ = sizeX * sizeZ;
        int size = sizeX * sizeY * sizeZ;
        if (bitSet == null || bitSet.length() < size) {
            bitSet = new BitSet(size);
        } else {
            bitSet.clear();
        }
    }

    public void clear() {
        bitSet.clear();
    }

    public void set(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        bitSet.set(index);
    }

    public void unset(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        bitSet.set(index, false);
    }

    public boolean test(int x, int y, int z) {
        int index = indexOf(x - minX, y - minY, z - minZ);
        return bitSet.get(index);
    }

    private int indexOf(int x, int y, int z) {
        return (y * sizeXZ) + (z * sizeX) + x;
    }
}
