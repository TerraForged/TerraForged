package com.terraforged.mod.util.map;

public interface Index {
    Index CHUNK = (x, z) -> ((z & 15) << 4) | (x & 15);

    int of(int x, int z);

    static Index borderedChunk(int border) {
        return new Index() {
            private final int offset = border;
            private final int size = 16 + border * 2;

            @Override
            public int of(int x, int z) {
                x += offset;
                z += offset;
                return z * size + x;
            }
        };
    }
}
