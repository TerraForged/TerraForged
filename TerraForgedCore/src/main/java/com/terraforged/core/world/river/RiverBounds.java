package com.terraforged.core.world.river;

import me.dags.noise.util.Vec2f;

public class RiverBounds {

    private final int x1;
    private final int y1;
    private final int x2;
    private final int y2;
    private final int minX;
    private final int minY;
    private final int maxX;
    private final int maxY;

    public RiverBounds(int x1, int y1, int x2, int y2, int radius) {
        this.x1 = x1;
        this.y1 = y1;
        this.x2 = x2;
        this.y2 = y2;
        this.minX = Math.min(x1, x2) - radius;
        this.minY = Math.min(y1, y2) - radius;
        this.maxX = Math.max(x1, x2) + radius;
        this.maxY = Math.max(y1, y2) + radius;
    }

    public int x1() {
        return x1;
    }

    public int y1() {
        return y1;
    }

    public int x2() {
        return x2;
    }

    public int y2() {
        return y2;
    }

    public boolean overlaps(RiverBounds other) {
       if (minX > other.maxX || maxX < other.minX) {
           return false;
       }
       if (minY > other.maxY || maxY < other.minY) {
           return false;
       }
       return true;
    }

    public Vec2f pos(float distance) {
        int dx = x2() - x1();
        int dy = y2() - y1();
        return new Vec2f(x1() + dx * distance, y1() + dy * distance);
    }

    public static RiverBounds fromNodes(RiverNode p1, RiverNode p2) {
        if (p1.type == RiverNode.Type.START) {
            return new RiverBounds(p1.x, p1.z, p2.x, p2.z, 300);
        } else {
            return new RiverBounds(p2.x, p2.z, p1.x, p1.z, 300);
        }
    }
}
