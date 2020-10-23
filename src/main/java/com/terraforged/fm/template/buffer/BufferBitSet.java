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

    public void assign(int minX, int minY, int minZ, int sizeX, int sizeY, int sizeZ) {
        this.minX = minX;
        this.minY = minY;
        this.minZ = minZ;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
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
