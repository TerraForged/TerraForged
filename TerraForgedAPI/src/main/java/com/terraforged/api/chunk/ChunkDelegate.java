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

package com.terraforged.api.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.IFluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

public class ChunkDelegate implements ChunkAccess {

    private final IChunk chunk;

    public ChunkDelegate(IChunk chunk) {
        this.chunk = chunk;
    }

    public IChunk getChunk() {
        return chunk;
    }

    @Override
    public BlockState getState(BlockPos pos) {
        return chunk.getBlockState(pos);
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return chunk.setBlockState(pos, state, isMoving);
    }

    @Override
    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
        chunk.addTileEntity(pos, tileEntityIn);
    }

    @Override
    public void addEntity(Entity entityIn) {
        chunk.addEntity(entityIn);
    }

    @Override
    @Nullable
    public ChunkSection getLastExtendedBlockStorage() {
        return chunk.getLastExtendedBlockStorage();
    }

    @Override
    public int getTopFilledSegment() {
        return chunk.getTopFilledSegment();
    }

    @Override
    public Set<BlockPos> getTileEntitiesPos() {
        return chunk.getTileEntitiesPos();
    }

    @Override
    public ChunkSection[] getSections() {
        return chunk.getSections();
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return chunk.getHeightmaps();
    }

    @Override
    public void setHeightmap(Heightmap.Type type, long[] data) {
        chunk.setHeightmap(type, data);
    }

    @Override
    public Heightmap getHeightmap(Heightmap.Type type) {
        return chunk.getHeightmap(type);
    }

    @Override
    public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
        return chunk.getTopBlockY(heightmapType, x, z);
    }

    @Override
    public ChunkPos getPos() {
        return chunk.getPos();
    }

    @Override
    public void setLastSaveTime(long saveTime) {
        chunk.setLastSaveTime(saveTime);
    }

    @Override
    public Map<String, StructureStart> getStructureStarts() {
        return chunk.getStructureStarts();
    }

    @Override
    public void setStructureStarts(Map<String, StructureStart> structureStartsIn) {
        chunk.setStructureStarts(structureStartsIn);
    }

    @Override
    public boolean isEmptyBetween(int startY, int endY) {
        return chunk.isEmptyBetween(startY, endY);
    }

    @Override
    @Nullable
    public BiomeContainer getBiomes() {
        return chunk.getBiomes();
    }

    @Override
    public void setModified(boolean modified) {
        chunk.setModified(modified);
    }

    @Override
    public boolean isModified() {
        return chunk.isModified();
    }

    @Override
    public ChunkStatus getStatus() {
        return chunk.getStatus();
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        chunk.removeTileEntity(pos);
    }

    @Override
    public void markBlockForPostprocessing(BlockPos pos) {
        chunk.markBlockForPostprocessing(pos);
    }

    @Override
    public ShortList[] getPackedPositions() {
        return chunk.getPackedPositions();
    }

    @Override
    public void func_201636_b(short packedPosition, int index) {
        chunk.func_201636_b(packedPosition, index);
    }

    @Override
    public void addTileEntity(CompoundNBT nbt) {
        chunk.addTileEntity(nbt);
    }

    @Override
    @Nullable
    public CompoundNBT getDeferredTileEntity(BlockPos pos) {
        return chunk.getDeferredTileEntity(pos);
    }

    @Override
    @Nullable
    public CompoundNBT getTileEntityNBT(BlockPos pos) {
        return chunk.getTileEntityNBT(pos);
    }

    @Override
    public Stream<BlockPos> getLightSources() {
        return chunk.getLightSources();
    }

    @Override
    public ITickList<Block> getBlocksToBeTicked() {
        return chunk.getBlocksToBeTicked();
    }

    @Override
    public ITickList<Fluid> getFluidsToBeTicked() {
        return chunk.getFluidsToBeTicked();
    }

    @Override
    public BitSet getCarvingMask(GenerationStage.Carving type) {
        return chunk.getCarvingMask(type);
    }

    @Override
    public UpgradeData getUpgradeData() {
        return chunk.getUpgradeData();
    }

    @Override
    public void setInhabitedTime(long newInhabitedTime) {
        chunk.setInhabitedTime(newInhabitedTime);
    }

    @Override
    public long getInhabitedTime() {
        return chunk.getInhabitedTime();
    }

    public static ShortList getList(ShortList[] p_217308_0_, int p_217308_1_) {
        return IChunk.getList(p_217308_0_, p_217308_1_);
    }

    @Override
    public boolean hasLight() {
        return chunk.hasLight();
    }

    @Override
    public void setLight(boolean p_217305_1_) {
        chunk.setLight(p_217305_1_);
    }

    @Override
    @Nullable
    public IWorld getWorldForge() {
        return chunk.getWorldForge();
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return chunk.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return chunk.getBlockState(pos);
    }

    @Override
    public IFluidState getFluidState(BlockPos pos) {
        return chunk.getFluidState(pos);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return chunk.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return chunk.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return chunk.getHeight();
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return chunk.rayTraceBlocks(context);
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTraceBlocks(Vec3d p_217296_1_, Vec3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return chunk.rayTraceBlocks(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    public static <T> T func_217300_a(RayTraceContext p_217300_0_, BiFunction<RayTraceContext, BlockPos, T> p_217300_1_, Function<RayTraceContext, T> p_217300_2_) {
        return IBlockReader.func_217300_a(p_217300_0_, p_217300_1_, p_217300_2_);
    }

    @Override
    @Nullable
    public StructureStart getStructureStart(String stucture) {
        return chunk.getStructureStart(stucture);
    }

    @Override
    public void putStructureStart(String structureIn, StructureStart structureStartIn) {
        chunk.putStructureStart(structureIn, structureStartIn);
    }

    @Override
    public LongSet getStructureReferences(String structureIn) {
        return chunk.getStructureReferences(structureIn);
    }

    @Override
    public void addStructureReference(String strucutre, long reference) {
        chunk.addStructureReference(strucutre, reference);
    }

    @Override
    public Map<String, LongSet> getStructureReferences() {
        return chunk.getStructureReferences();
    }

    @Override
    public void setStructureReferences(Map<String, LongSet> p_201606_1_) {
        chunk.setStructureReferences(p_201606_1_);
    }
}
