package com.terraforged.core.world.river;

import com.terraforged.core.world.heightmap.Heightmap;
import me.dags.noise.domain.Domain;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;
import me.dags.noise.util.Vec2i;
import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class RiverRegion {

    public static final int SCALE = 12;
//    private static final float LAKE_CHANCE = 0.05F;
//    private static final float LAKE_MIN_DIST = 0.025F;
//    private static final float LAKE_MAX_DIST = 0.05F;
//    private static final float LAKE_MIN_SIZE = 50;
//    private static final float LAKE_MAX_SIZE = 100;

    private final Domain domain;
    private final Terrains terrains;
    private final LakeConfig lake;
    private final RiverConfig primary;
    private final RiverConfig secondary;
    private final RiverConfig tertiary;

    private final List<River> rivers;
    private final List<Lake> lakes = new LinkedList<>();

    public RiverRegion(int regionX, int regionZ, Heightmap heightmap, GeneratorContext context, RiverConfig primary, RiverConfig secondary, RiverConfig tertiary, LakeConfig lake) {
        int seed = new Random(NoiseUtil.seed(regionX, regionZ)).nextInt();
        this.lake = lake;
        this.primary = primary;
        this.secondary = secondary;
        this.tertiary = tertiary;
        this.terrains = context.terrain;
        this.domain = Domain.warp(seed, 400, 1, 400).add(Domain.warp(seed + 1, 50, 1, 25));
        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            PosGenerator pos = new PosGenerator(heightmap, domain, cell.getValue(),1 << SCALE, River.VALLEY_WIDTH);
            this.rivers = generate(regionX, regionZ, pos);
        }
    }

    public void apply(Cell<Terrain> cell, float x, float z) {
        float px = domain.getX(x, z);
        float pz = domain.getY(x, z);
        for (River river : rivers) {
            river.apply(cell, px, pz);
        }
        for (Lake lake : lakes) {
            lake.apply(cell, px, pz);
        }
    }

    private List<River> generate(int regionX, int regionZ, PosGenerator pos) {
        int x = regionToBlock(regionX);
        int z = regionToBlock(regionZ);
        long regionSeed = NoiseUtil.seed(regionX, regionZ);

        Random random = new Random(regionSeed);
        List<River> rivers = new LinkedList<>();

        // generates main rivers until either 10 attempts have passed or 2 rivers generate
        for (int i = 0; rivers.size() < 2 && i < 50; i++) {
            generateRiver(x, z, pos, primary, random, rivers);
        }

        for (int i = 0; rivers.size() < 10 && i < 15; i++) {
            generateRiver(x, z, pos, secondary, random, rivers);
        }

        for (int i = 0; rivers.size() < 10 && i < 15; i++) {
            generateConnectingRiver(x, z, pos, tertiary, random, rivers);
        }

        for (int i = 0; rivers.size() < 20 && i < 40; i++) {
            generateRiver(x, z, pos, tertiary, random, rivers);
        }


        Collections.reverse(rivers);

        return rivers;
    }

    /**
     * Attempts to generate a line that starts inland and reaches the ocean
     */
    private boolean generateRiver(int x, int z, PosGenerator pos, RiverConfig config, Random random, List<River> rivers) {
        // generate either a river start or end node
        RiverNode p1 = pos.next(x, z, random, 50);
        if (p1 == null) {
            return false;
        }

        // generate a node with a min distance from p1 and that has the opposite node type to p1
        RiverNode p2 = pos.nextFrom(x, z, random,50, p1, config.length2);
        if (p2 == null) {
            return false;
        }

        // avoid collisions with existing rivers
        RiverBounds bounds = RiverBounds.fromNodes(p1, p2);
        for (River river : rivers) {
            if (bounds.overlaps(river.bounds)) {
                return false;
            }
        }

        generateLake(bounds, random);

        return rivers.add(new River(bounds, config, terrains, config.fade, 0));
    }

    /**
     * Attempts to generate an inland position (p1), finds the nearest river (AB) to it, and tries to connect
     * a line from p1 to some position along AB
     */
    private boolean generateConnectingRiver(int x, int z, PosGenerator pos, RiverConfig config, Random random, List<River> rivers) {
        // generate a river start node
        RiverNode p1 = pos.nextType(x, z, random, 50, RiverNode.Type.START);
        if (p1 == null) {
            return false;
        }

        // find closest river
        River closest = null;
        float startDist2 = Integer.MAX_VALUE;
        for (River river : rivers) {
            float d2 = dist2(p1.x, p1.z, river.bounds.x1(), river.bounds.y1());
            if (d2 < startDist2) {
                startDist2 = d2;
                closest = river;
            }
        }

        // no close rivers
        if (closest == null) {
            return false;
        }

        // p1 is too close to start of the closest river
        if (startDist2 < 640000) {
            return false;
        }

        // p1 should be closer to the main river start than it's end, otherwise we're likely to get forks
        // in the wrong direction (ie one river splitting into two, rather two rivers joining)
        float endDist2 = dist2(p1.x, p1.z, closest.bounds.x2(), closest.bounds.y2());
        if (endDist2 < startDist2) {
            return false;
        }

        // pick a point some random distance along the main river, between 30%-70% along the main river length
        // 0.4 offset should ensure the main river has become sufficiently wide for it not to look weird having
        // a second river connect to it
        float dist = 0.4F + (random.nextFloat() * 0.3F);
        Vec2i p2 = closest.bounds.pos(dist).toInt();
        RiverBounds bounds = new RiverBounds(p1.x, p1.z, p2.x, p2.y, 300);

        // check that the new river does not clash/overlap any other nearby rivers
        for (River river : rivers) {
            if (river == closest) {
                continue;
            }
            if (river.bounds.overlaps(bounds)) {
                return false;
            }
        }

        generateLake(bounds, random);

        return rivers.add(new River(bounds, config, terrains, config.fade, 0, true));
    }

    private void generateLake(RiverBounds bounds, Random random) {
        if (random.nextFloat() < lake.chance) {
            float size = lake.sizeMin + (random.nextFloat() * lake.sizeMax);
            float distance = lake.distanceMin + (random.nextFloat() * lake.distanceMax);
            Vec2f center = bounds.pos(distance);
            lakes.add(new Lake(center, size, lake, terrains));
        }
    }

    private static float dist2(float x1, float y1, float x2, float y2) {
        float dx = x2 - x1;
        float dy = y2 - y1;
        return dx * dx + dy * dy;
    }

    public static int regionToBlock(int value) {
        return value << SCALE;
    }

    public static int blockToRegion(int value) {
        return value >> SCALE;
    }
}
