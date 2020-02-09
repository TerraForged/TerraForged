package com.terraforged.core.world.river;

import com.terraforged.core.world.heightmap.Heightmap;
import me.dags.noise.domain.Domain;
import me.dags.noise.util.Vec2i;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.world.terrain.Terrain;

import java.util.Random;

/**
 * Generates random positions within (length / 4) of one of the 4 corners of a region.
 * The first position generated will be near any of the four corners
 * The second position generated will be near any of the 3 remaining corners to ensure a reasonable distance to the first
 */
public class PosGenerator {

    private final int quadSize;
    private final Vec2i[] quads = new Vec2i[4];

    private final int padding;
    private final Domain domain;
    private final Cell<Terrain> lookup;
    private final Heightmap heightmap;

    private int i;
    private int dx;
    private int dz;

    public PosGenerator(Heightmap heightmap, Domain domain, Cell<Terrain> lookup, int size, int padding) {
        this.domain = domain;
        this.lookup = lookup;
        this.padding = padding;
        this.heightmap = heightmap;
        this.quadSize = (size - (padding * 2)) / 4;
        int x1 = 0;
        int y1 = 0;
        int x2 = 3 * quadSize;
        int y2 = 3 * quadSize;
        quads[index(0, 0)] = new Vec2i(x1, y1);
        quads[index(1, 0)] = new Vec2i(x2, y1);
        quads[index(0, 1)] = new Vec2i(x1, y2);
        quads[index(1, 1)] = new Vec2i(x2, y2);
    }

    private void nextSeed(Random random) {
        int index = random.nextInt(4);
        Vec2i vec = quads[index];
        i = index;
        dx = padding + vec.x + random.nextInt(quadSize);
        dz = padding + vec.y + random.nextInt(quadSize);
    }

    private void nextPos(Random random) {
        int steps = 1 + random.nextInt(3);
        int index = (i + steps) & 3;
        Vec2i vec = quads[index];
        i = index;
        dx = padding + vec.x + random.nextInt(quadSize);
        dz = padding + vec.y + random.nextInt(quadSize);
    }

    public RiverNode next(int x, int z, Random random, int attempts) {
        for (int i = 0; i < attempts; i++) {
            nextSeed(random);
            int px = x + dx;
            int pz = z + dz;
            int wx = (int) domain.getX(px, pz);
            int wz = (int) domain.getY(px, pz);
            float value1 = getHeight(px, pz);
            float value2 = getHeight(wx, wz);
            RiverNode.Type type1 = RiverNode.getType(value1);
            RiverNode.Type type2 = RiverNode.getType(value2);
            if (type1 == type2 && type1 != RiverNode.Type.NONE) {
                if (type1 == RiverNode.Type.END) {
                    return new RiverNode(wx, wz, type1);
                }
                return new RiverNode(px, pz, type1);
            }
        }
        return null;
    }

    public RiverNode nextFrom(int x, int z, Random random, int attempts, RiverNode point, int mindDist2) {
        for (int i = 0; i < attempts; i++) {
            nextPos(random);
            int px = x + dx;
            int pz = z + dz;
            if (dist2(px, pz, point.x, point.z) < mindDist2) {
                continue;
            }
            int wx = (int) domain.getX(px, pz);
            int wz = (int) domain.getY(px, pz);
            float value1 = getHeight(px, pz);
            float value2 = getHeight(wx, wz);
            RiverNode.Type type1 = RiverNode.getType(value1);
            RiverNode.Type type2 = RiverNode.getType(value2);
            if (type1 == type2 && type1 == point.type.opposite()) {
                if (type1 == RiverNode.Type.END) {
                    return new RiverNode(wx, wz, type1);
                }
                return new RiverNode(px, pz, type1);
            }
        }
        return null;
    }

    public RiverNode nextType(int x, int z, Random random, int attempts, RiverNode.Type match) {
        for (int i = 0; i < attempts; i++) {
            nextSeed(random);
            int px = x + dx;
            int pz = z + dz;
            int wx = (int) domain.getX(px, pz);
            int wz = (int) domain.getY(px, pz);
            float value1 = getHeight(px, pz);
            float value2 = getHeight(wx, wz);
            RiverNode.Type type1 = RiverNode.getType(value1);
            RiverNode.Type type2 = RiverNode.getType(value2);
            if (type1 == type2 && type1 == match) {
                return new RiverNode(px, pz, type1);
            }
        }
        return null;
    }

    public RiverNode nextMinHeight(int x, int z, Random random, int attempts, float minHeight) {
        for (int i = 0; i < attempts; i++) {
            nextPos(random);
            int px = x + dx;
            int pz = z + dz;
            int wx = (int) domain.getX(px, pz);
            int wz = (int) domain.getY(px, pz);
            float value1 = getHeight(px, pz);
            float value2 = getHeight(wx, wz);
            if (value1 > minHeight && value2 > minHeight) {
                return new RiverNode(px, pz, RiverNode.Type.START);
            }
        }
        return null;
    }

    private float getHeight(int x, int z) {
        heightmap.visit(lookup, x, z);
        return lookup.value;
    }

    private static int index(int x, int z) {
        return z * 2 + x;
    }

    private static float dist2(int x1, int y1, int x2, int y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }
}
