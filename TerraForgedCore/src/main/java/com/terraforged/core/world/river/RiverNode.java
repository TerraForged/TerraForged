package com.terraforged.core.world.river;

public class RiverNode {

    public final int x;
    public final int z;
    public final Type type;

    public RiverNode(int x, int z, Type type) {
        this.x = x;
        this.z = z;
        this.type = type;
    }

    public static Type getType(float value) {
        if (River.validStart(value)) {
            return Type.START;
        }
        if (River.validEnd(value)) {
            return Type.END;
        }
        return Type.NONE;
    }

    public enum Type {
        NONE {
            @Override
            public Type opposite() {
                return this;
            }
        },
        START {
            @Override
            public Type opposite() {
                return END;
            }
        },
        END {
            @Override
            public Type opposite() {
                return START;
            }
        },
        ;

        public abstract Type opposite();
    }
}
