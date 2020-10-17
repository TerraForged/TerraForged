package com.terraforged.mod.feature.decorator.fastpoisson;

import com.terraforged.n2d.Source;
import com.terraforged.n2d.util.NoiseUtil;
import com.terraforged.n2d.util.Vec2f;
import it.unimi.dsi.fastutil.longs.*;
import net.minecraft.util.math.ChunkPos;

import javax.swing.*;
import java.awt.image.BufferedImage;
import java.util.Random;

public class FastPoisson {

    public static final ThreadLocal<FastPoisson> LOCAL_POISSON = ThreadLocal.withInitial(FastPoisson::new);

    private final LongArraySet region = new LongArraySet();
    private final LongArrayList chunk = new LongArrayList();

    public  <Ctx> void visit(int seed, int chunkX, int chunkZ, Random random, FastPoissonContext context, Ctx ctx, Visitor<Ctx> visitor) {
        region.clear();
        chunk.clear();
        visit(seed, chunkX, chunkZ, random, context, region, chunk, ctx, visitor);
    }

    public static <Ctx> void visit(int seed, int chunkX, int chunkZ, Random random, FastPoissonContext context, LongArraySet region, LongArrayList chunk, Ctx ctx, Visitor<Ctx> visitor) {
        int startX = chunkX << 4;
        int startZ = chunkZ << 4;

        // iterate region around chunk to collect nearby points
        collectPoints(seed, startX, startZ, context, region, chunk);

        // shuffle to disguise iteration order bias
        LongLists.shuffle(chunk, random);

        // iterate chunk points and check for validity before passing to the visitor
        visitPoints(startX, startZ, region, chunk, context, ctx, visitor);
    }

    private static void collectPoints(int seed, int startX, int startZ, FastPoissonContext context, LongArraySet region, LongArrayList chunk) {
        int min = -4;
        int max = 16 + 8;
        for (int dz = min; dz < max; dz++) {
            for (int dx = min; dx < max; dx++) {
                int x = startX + dx;
                int z = startZ + dz;

                long point = getPoint(seed, context.frequency, x, z);
                int px = unpackX(point);
                int pz = unpackZ(point);

                if (px < startX || pz < startZ) {
                    continue;
                }

                if (region.add(point) && inChunkBounds(px, pz, startX, startZ)) {
                    chunk.add(point);
                }
            }
        }
    }

    private static <Ctx> void visitPoints(int startX, int startZ, LongSet region, LongList chunk, FastPoissonContext context, Ctx ctx, Visitor<Ctx> visitor) {
        for (long point : chunk) {
            int px = unpackX(point);
            int pz = unpackZ(point);

            // point has been culled already
            if (!region.contains(point)) {
                continue;
            }

            float noise = context.density.getValue(px, pz);
            float radius2 = context.radius2 * noise;
            if (checkNeighbours(startX, startZ, point, px, pz, radius2, region)) {
                visitor.visit(px, pz, ctx);
            }
        }
    }

    private static boolean checkNeighbours(int startX, int startZ, long point, int x, int z, float radius2, LongSet region) {
        LongIterator iterator = region.iterator();
        while (iterator.hasNext()) {
            long neighbour = iterator.nextLong();
            // ignore self
            if (point == neighbour) {
                continue;
            }

            int px = unpackX(neighbour);
            int pz = unpackZ(neighbour);

            // ignore if further than min distance away
            if (dist2(x, z, px, pz) > radius2) {
                continue;
            }

            // neighbour lies outside of the chunk so can't be removed
            // point is instead marked as invalid and discarded
            if (!inChunkBounds(px, pz, startX, startZ)) {
                return false;
            }

            // safe to cull neighbour
            iterator.remove();
        }
        return true;
    }

    private static long getPoint(int seed, float frequency, float x, float z) {
        x *= frequency;
        z *= frequency;

        int cellX = (int) x;
        int cellZ = (int) z;
        Vec2f vec = NoiseUtil.CELL_2D[NoiseUtil.hash2D(seed, cellX, cellZ) & 255];

        // vec range is -0.5 to +0.5
        int px = (int) ((cellX + 0.5F + vec.x) / frequency);
        int pz = (int) ((cellZ + 0.5F + vec.y) / frequency);

        return pack(px, pz);
    }

    private static boolean inChunkBounds(int px, int pz, int startX, int startZ) {
        int dx = px - startX;
        int dz = pz - startZ;
        return dx > -1 && dx < 16 && dz > -1 && dz < 16;
    }

    private static int dist2(int ax, int az, int bx, int bz) {
        int dx = ax - bx;
        int dz = az - bz;
        return dx * dx + dz * dz;
    }

    private static long pack(int x, int y) {
        return ChunkPos.asLong(x, y);
    }

    private static int unpackX(long l) {
        return ChunkPos.getX(l);
    }

    private static int unpackZ(long l) {
        return ChunkPos.getZ(l);
    }

    public interface Visitor<Ctx> {

        void visit(int x, int z, Ctx ctx);
    }
}
