/*
 *   
 * MIT License
 *
 * Copyright (c) 2020 TerraForged
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.terraforged.core.world.river;

import com.terraforged.core.cell.Cell;
import com.terraforged.core.util.concurrent.ObjectPool;
import com.terraforged.core.util.concurrent.cache.ExpiringEntry;
import com.terraforged.core.world.GeneratorContext;
import com.terraforged.core.world.heightmap.Heightmap;
import com.terraforged.core.world.terrain.Terrain;
import com.terraforged.core.world.terrain.Terrains;
import me.dags.noise.domain.Domain;
import me.dags.noise.util.NoiseUtil;
import me.dags.noise.util.Vec2f;
import me.dags.noise.util.Vec2i;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RiverRegion implements ExpiringEntry {

    public static final int SCALE = 12;
    private static final double MIN_FORK_ANGLE = 20D;
    private static final double MAX_FORK_ANGLE = 60D;
    private static final long EXPIRE_TIME = TimeUnit.SECONDS.toMillis(60);

    private final Domain domain;
    private final Terrains terrains;
    private final LakeConfig lake;
    private final RiverConfig primary;
    private final RiverConfig secondary;
    private final RiverConfig tertiary;

    private final List<River> rivers;
    private final List<Lake> lakes = new LinkedList<>();

    private final int primaryLimit;
    private final int secondaryLimit;
    private final int secondaryRelaxedLimit;
    private final int forkLimit;
    private final int tertiaryLimit;
    private final int primaryAttempts;
    private final int secondaryAttempts;
    private final int secondaryRelaxedAttempts;
    private final int forkAttempts;
    private final int tertiaryAttempts;

    private final long timestamp = System.currentTimeMillis() + EXPIRE_TIME;

    public RiverRegion(int regionX, int regionZ, Heightmap heightmap, GeneratorContext context, RiverContext riverContext) {
        int seed = new Random(NoiseUtil.seed(regionX, regionZ)).nextInt();
        this.lake = riverContext.lakes;
        this.primary = riverContext.primary;
        this.secondary = riverContext.secondary;
        this.tertiary = riverContext.tertiary;
        this.terrains = context.terrain;
        this.domain = Domain.warp(seed, 400, 1, 400)
                .add(Domain.warp(seed + 1, 80, 1, 35));

        this.primaryLimit = NoiseUtil.round(10 * riverContext.frequency);
        this.secondaryLimit = NoiseUtil.round(20 * riverContext.frequency);
        this.secondaryRelaxedLimit = NoiseUtil.round(30 * riverContext.frequency);
        this.forkLimit = NoiseUtil.round(40 * riverContext.frequency);
        this.tertiaryLimit = NoiseUtil.round(50 * riverContext.frequency);

        this.primaryAttempts = NoiseUtil.round(100 * riverContext.frequency);
        this.secondaryAttempts = NoiseUtil.round(100 * riverContext.frequency);
        this.secondaryRelaxedAttempts = NoiseUtil.round(50 * riverContext.frequency);
        this.forkAttempts = NoiseUtil.round(75 * riverContext.frequency);
        this.tertiaryAttempts = NoiseUtil.round(50 * riverContext.frequency);

        try (ObjectPool.Item<Cell<Terrain>> cell = Cell.pooled()) {
            PosGenerator pos = new PosGenerator(heightmap, domain, cell.getValue(),1 << SCALE, River.VALLEY_WIDTH);
            this.rivers = generate(regionX, regionZ, pos);
        }
    }

    @Override
    public long getTimestamp() {
        return timestamp;
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

        for (int i = 0; rivers.size() < primaryLimit && i < primaryAttempts; i++) {
            generateRiver(x, z, pos, primary, random, rivers);
        }

        for (int i = 0; rivers.size() < secondaryLimit && i < secondaryAttempts; i++) {
            generateRiver(x, z, pos, secondary, random, rivers);
        }

        for (int i = 0; rivers.size() < secondaryRelaxedLimit && i < secondaryRelaxedAttempts; i++) {
            generateRiverRelaxed(x, z, pos, secondary, random, rivers);
        }

        for (int i = 0; rivers.size() < forkLimit && i < forkAttempts; i++) {
            generateRiverFork(x, z, pos, tertiary, random, rivers);
        }

        for (int i = 0; rivers.size() < tertiaryLimit && i < tertiaryAttempts; i++) {
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
    private boolean generateRiverFork(int x, int z, PosGenerator pos, RiverConfig config, Random random, List<River> rivers) {
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

        // connection should occur no less than 20% along the river length and no greater than 40%
        float distance = 0.3F + (random.nextFloat() * 0.3F);
        Vec2i fork = closest.bounds.pos(distance).toInt();

        // connection should not occur in the ocean
        float height = pos.getHeight(fork.x, fork.y);
        RiverNode.Type type = RiverNode.getType(height);
        if (type == RiverNode.Type.END) {
            return false;
        }

        // calc angle between vector a (AB) & b (CB)
        // A - existing river's start
        // B - connection point
        // C - new river's start
        int ax = fork.x - closest.bounds.x1();
        int az = fork.y - closest.bounds.y1();
        int bx = fork.x - p1.x;
        int bz = fork.y - p1.z;

        // radians = arccos(AB.BC / |AB||BC|)
        int dotAB = ax * bx + az * bz;
        double lenA = Math.sqrt(ax * ax + az * az);
        double lenB = Math.sqrt(bx * bx + bz * bz);
        double radians = Math.acos(dotAB / (lenA * lenB));
        double angle = Math.toDegrees(radians);
        if (angle < MIN_FORK_ANGLE || angle > MAX_FORK_ANGLE) {
            return false;
        }

        RiverBounds bounds = new RiverBounds(p1.x, p1.z, fork.x, fork.y, 300);
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

        // scale the connecting river's width down so that it's narrower than the one it's connecting to
        float forkWidth = closest.config.bankWidth * distance * 0.6F;
        RiverConfig forkConfig = config.createFork(forkWidth);
        
        return rivers.add(new River(bounds, forkConfig, terrains, forkConfig.fade, 0, true));
    }

    private boolean generateRiverRelaxed(int x, int z, PosGenerator pos, RiverConfig config, Random random, List<River> rivers) {
        // generate either a river start or end node
        RiverNode p1 = pos.nextRelaxed(x, z, random, 50);
        if (p1 == null) {
            return false;
        }

        // generate a node with a min distance from p1 and that has the opposite node type to p1
        RiverNode p2 = pos.nextFromRelaxed(x, z, random,50, p1, config.length2);
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
