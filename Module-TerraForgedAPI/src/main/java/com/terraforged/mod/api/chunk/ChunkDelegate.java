/*
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

package com.terraforged.mod.api.chunk;

import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.fastutil.shorts.ShortList;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.*;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.palette.UpgradeData;
import net.minecraft.world.ITickList;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.BiomeContainer;
import net.minecraft.world.chunk.ChunkSection;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.structure.Structure;
import net.minecraft.world.gen.feature.structure.StructureStart;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ChunkDelegate implements IChunk {

    protected final IChunk delegate;

    public ChunkDelegate(IChunk delegate) {
        this.delegate = delegate;
    }

    public IChunk getDelegate() {
        return delegate;
    }

    @Override
    @Nullable
    public BlockState setBlockState(BlockPos pos, BlockState state, boolean isMoving) {
        return delegate.setBlockState(pos, state, isMoving);
    }

    @Override
    public void addTileEntity(BlockPos pos, TileEntity tileEntityIn) {
        delegate.addTileEntity(pos, tileEntityIn);
    }

    @Override
    public void addEntity(Entity entityIn) {
        delegate.addEntity(entityIn);
    }

    @Override
    @Nullable
    public ChunkSection getLastExtendedBlockStorage() {
        return delegate.getLastExtendedBlockStorage();
    }

    @Override
    public int getTopFilledSegment() {
        return delegate.getTopFilledSegment();
    }

    @Override
    public Set<BlockPos> getTileEntitiesPos() {
        return delegate.getTileEntitiesPos();
    }

    @Override
    public ChunkSection[] getSections() {
        return delegate.getSections();
    }

    @Override
    public Collection<Map.Entry<Heightmap.Type, Heightmap>> getHeightmaps() {
        return delegate.getHeightmaps();
    }

    @Override
    public void setHeightmap(Heightmap.Type type, long[] data) {
        delegate.setHeightmap(type, data);
    }

    @Override
    public Heightmap getHeightmap(Heightmap.Type typeIn) {
        return delegate.getHeightmap(typeIn);
    }

    @Override
    public int getTopBlockY(Heightmap.Type heightmapType, int x, int z) {
        return delegate.getTopBlockY(heightmapType, x, z);
    }

    @Override
    public ChunkPos getPos() {
        return delegate.getPos();
    }

    @Override
    public void setLastSaveTime(long saveTime) {
        delegate.setLastSaveTime(saveTime);
    }

    @Override
    public Map<Structure<?>, StructureStart<?>> getStructureStarts() {
        return delegate.getStructureStarts();
    }

    @Override
    public void setStructureStarts(Map<Structure<?>, StructureStart<?>> structureStartsIn) {
        delegate.setStructureStarts(structureStartsIn);
    }

    @Override
    public boolean isEmptyBetween(int startY, int endY) {
        return delegate.isEmptyBetween(startY, endY);
    }

    @Override
    @Nullable
    public BiomeContainer getBiomes() {
        return delegate.getBiomes();
    }

    @Override
    public void setModified(boolean modified) {
        delegate.setModified(modified);
    }

    @Override
    public boolean isModified() {
        return delegate.isModified();
    }

    @Override
    public ChunkStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public void removeTileEntity(BlockPos pos) {
        delegate.removeTileEntity(pos);
    }

    @Override
    public void markBlockForPostprocessing(BlockPos pos) {
        delegate.markBlockForPostprocessing(pos);
    }

    @Override
    public ShortList[] getPackedPositions() {
        return delegate.getPackedPositions();
    }

    @Override
    public void addPackedPosition(short packedPosition, int index) {
        delegate.addPackedPosition(packedPosition, index);
    }

    @Override
    public void addTileEntity(CompoundNBT nbt) {
        delegate.addTileEntity(nbt);
    }

    @Override
    @Nullable
    public CompoundNBT getDeferredTileEntity(BlockPos pos) {
        return delegate.getDeferredTileEntity(pos);
    }

    @Override
    @Nullable
    public CompoundNBT getTileEntityNBT(BlockPos pos) {
        return delegate.getTileEntityNBT(pos);
    }

    @Override
    public Stream<BlockPos> getLightSources() {
        return delegate.getLightSources();
    }

    @Override
    public ITickList<Block> getBlocksToBeTicked() {
        return delegate.getBlocksToBeTicked();
    }

    @Override
    public ITickList<Fluid> getFluidsToBeTicked() {
        return delegate.getFluidsToBeTicked();
    }

    @Override
    public UpgradeData getUpgradeData() {
        return delegate.getUpgradeData();
    }

    @Override
    public void setInhabitedTime(long newInhabitedTime) {
        delegate.setInhabitedTime(newInhabitedTime);
    }

    @Override
    public long getInhabitedTime() {
        return delegate.getInhabitedTime();
    }

    @Override
    public boolean hasLight() {
        return delegate.hasLight();
    }

    @Override
    public void setLight(boolean lightCorrectIn) {
        delegate.setLight(lightCorrectIn);
    }

    @Override
    @Nullable
    public IWorld getWorldForge() {
        return delegate.getWorldForge();
    }

    @Override
    @Nullable
    public TileEntity getTileEntity(BlockPos pos) {
        return delegate.getTileEntity(pos);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return delegate.getBlockState(pos);
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return delegate.getFluidState(pos);
    }

    @Override
    public int getLightValue(BlockPos pos) {
        return delegate.getLightValue(pos);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public int getHeight() {
        return delegate.getHeight();
    }

    @Override
    public Stream<BlockState> func_234853_a_(AxisAlignedBB p_234853_1_) {
        return delegate.func_234853_a_(p_234853_1_);
    }

    @Override
    public BlockRayTraceResult rayTraceBlocks(RayTraceContext context) {
        return delegate.rayTraceBlocks(context);
    }

    @Override
    @Nullable
    public BlockRayTraceResult rayTraceBlocks(Vector3d startVec, Vector3d endVec, BlockPos pos, VoxelShape shape, BlockState state) {
        return delegate.rayTraceBlocks(startVec, endVec, pos, shape, state);
    }

    @Override
    public double func_242402_a(VoxelShape p_242402_1_, Supplier<VoxelShape> p_242402_2_) {
        return delegate.func_242402_a(p_242402_1_, p_242402_2_);
    }

    @Override
    public double func_242403_h(BlockPos p_242403_1_) {
        return delegate.func_242403_h(p_242403_1_);
    }

    @Override
    @Nullable
    public StructureStart<?> func_230342_a_(Structure<?> p_230342_1_) {
        return delegate.func_230342_a_(p_230342_1_);
    }

    @Override
    public void func_230344_a_(Structure<?> p_230344_1_, StructureStart<?> p_230344_2_) {
        delegate.func_230344_a_(p_230344_1_, p_230344_2_);
    }

    @Override
    public LongSet func_230346_b_(Structure<?> p_230346_1_) {
        return delegate.func_230346_b_(p_230346_1_);
    }

    @Override
    public void func_230343_a_(Structure<?> p_230343_1_, long p_230343_2_) {
        delegate.func_230343_a_(p_230343_1_, p_230343_2_);
    }

    @Override
    public Map<Structure<?>, LongSet> getStructureReferences() {
        return delegate.getStructureReferences();
    }

    @Override
    public void setStructureReferences(Map<Structure<?>, LongSet> structureReferences) {
        delegate.setStructureReferences(structureReferences);
    }
}