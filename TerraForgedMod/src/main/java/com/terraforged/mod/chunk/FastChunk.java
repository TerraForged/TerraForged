package com.terraforged.mod.chunk;

import com.terraforged.api.chunk.ChunkDelegate;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkPrimer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;

/**
 * A ChunkPrimer wrapper that handles setting BlockStates within the chunk & updating heightmaps accordingly
 */
public class FastChunk implements ChunkDelegate {

    private final int blockX;
    private final int blockZ;
    private final ChunkPrimer primer;
    private final Heightmap worldSurface;
    private final Heightmap oceanSurface;
    private final BlockPos.Mutable mutable = new BlockPos.Mutable();

    private FastChunk(ChunkPrimer primer) {
        this.primer = primer;
        this.blockX = primer.getPos().getXStart();
        this.blockZ = primer.getPos().getZStart();
        this.worldSurface = primer.getHeightmap(Heightmap.Type.WORLD_SURFACE_WG);
        this.oceanSurface = primer.getHeightmap(Heightmap.Type.OCEAN_FLOOR_WG);
    }

    @Override
    public IChunk getDelegate() {
        return primer;
    }

    @Override
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean falling) {
        if (pos.getY() >= 0 && pos.getY() < 256) {
            ChunkSection section = primer.getSection(pos.getY() >> 4);
            section.lock();
            int dx = pos.getX() & 15;
            int dy = pos.getY() & 15;
            int dz = pos.getZ() & 15;
            BlockState replaced = section.setBlockState(dx, dy, dz, state, false);
            if (state.getBlock() != Blocks.AIR) {
                mutable.setPos(blockX + dx, pos.getY(), blockZ + dz);
                if (state.getLightValue(primer, mutable) != 0) {
                    primer.addLightPosition(mutable);
                }
                worldSurface.update(dx, pos.getY(), dz, state);
                oceanSurface.update(dx, pos.getY(), dz, state);
            }
            section.unlock();
            return replaced;
        }
        return Blocks.VOID_AIR.getDefaultState();
    }

    public void setBiomes(BiomeContainer biomes) {
        primer.func_225548_a_(biomes);
    }

    public static IChunk wrap(IChunk chunk) {
        if (chunk instanceof FastChunk) {
            return chunk;
        }
        if (chunk instanceof ChunkPrimer) {
            return new FastChunk((ChunkPrimer) chunk);
        }
        return chunk;
    }
}
