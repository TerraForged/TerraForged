/*
 * MIT License
 *
 * Copyright (c) 2021 TerraForged
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
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.RayTraceContext;
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
    public BlockState setBlockState(BlockPos p_177436_1_, BlockState p_177436_2_, boolean p_177436_3_) {
        return delegate.setBlockState(p_177436_1_, p_177436_2_, p_177436_3_);
    }

    @Override
    public void setBlockEntity(BlockPos p_177426_1_, TileEntity p_177426_2_) {
        delegate.setBlockEntity(p_177426_1_, p_177426_2_);
    }

    @Override
    public void addEntity(Entity p_76612_1_) {
        delegate.addEntity(p_76612_1_);
    }

    @Override
    @Nullable
    public ChunkSection getHighestSection() {
        return delegate.getHighestSection();
    }

    @Override
    public int getHighestSectionPosition() {
        return delegate.getHighestSectionPosition();
    }

    @Override
    public Set<BlockPos> getBlockEntitiesPos() {
        return delegate.getBlockEntitiesPos();
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
    public void setHeightmap(Heightmap.Type p_201607_1_, long[] p_201607_2_) {
        delegate.setHeightmap(p_201607_1_, p_201607_2_);
    }

    @Override
    public Heightmap getOrCreateHeightmapUnprimed(Heightmap.Type p_217303_1_) {
        return delegate.getOrCreateHeightmapUnprimed(p_217303_1_);
    }

    @Override
    public int getHeight(Heightmap.Type p_201576_1_, int p_201576_2_, int p_201576_3_) {
        return delegate.getHeight(p_201576_1_, p_201576_2_, p_201576_3_);
    }

    @Override
    public ChunkPos getPos() {
        return delegate.getPos();
    }

    @Override
    public void setLastSaveTime(long p_177432_1_) {
        delegate.setLastSaveTime(p_177432_1_);
    }

    @Override
    public Map<Structure<?>, StructureStart<?>> getAllStarts() {
        return delegate.getAllStarts();
    }

    @Override
    public void setAllStarts(Map<Structure<?>, StructureStart<?>> p_201612_1_) {
        delegate.setAllStarts(p_201612_1_);
    }

    @Override
    public boolean isYSpaceEmpty(int p_76606_1_, int p_76606_2_) {
        return delegate.isYSpaceEmpty(p_76606_1_, p_76606_2_);
    }

    @Override
    @Nullable
    public BiomeContainer getBiomes() {
        return delegate.getBiomes();
    }

    @Override
    public void setUnsaved(boolean p_177427_1_) {
        delegate.setUnsaved(p_177427_1_);
    }

    @Override
    public boolean isUnsaved() {
        return delegate.isUnsaved();
    }

    @Override
    public ChunkStatus getStatus() {
        return delegate.getStatus();
    }

    @Override
    public void removeBlockEntity(BlockPos p_177425_1_) {
        delegate.removeBlockEntity(p_177425_1_);
    }

    @Override
    public void markPosForPostprocessing(BlockPos p_201594_1_) {
        delegate.markPosForPostprocessing(p_201594_1_);
    }

    @Override
    public ShortList[] getPostProcessing() {
        return delegate.getPostProcessing();
    }

    @Override
    public void addPackedPostProcess(short p_201636_1_, int p_201636_2_) {
        delegate.addPackedPostProcess(p_201636_1_, p_201636_2_);
    }

    @Override
    public void setBlockEntityNbt(CompoundNBT p_201591_1_) {
        delegate.setBlockEntityNbt(p_201591_1_);
    }

    @Override
    @Nullable
    public CompoundNBT getBlockEntityNbt(BlockPos p_201579_1_) {
        return delegate.getBlockEntityNbt(p_201579_1_);
    }

    @Override
    @Nullable
    public CompoundNBT getBlockEntityNbtForSaving(BlockPos p_223134_1_) {
        return delegate.getBlockEntityNbtForSaving(p_223134_1_);
    }

    @Override
    public Stream<BlockPos> getLights() {
        return delegate.getLights();
    }

    @Override
    public ITickList<Block> getBlockTicks() {
        return delegate.getBlockTicks();
    }

    @Override
    public ITickList<Fluid> getLiquidTicks() {
        return delegate.getLiquidTicks();
    }

    @Override
    public UpgradeData getUpgradeData() {
        return delegate.getUpgradeData();
    }

    @Override
    public void setInhabitedTime(long p_177415_1_) {
        delegate.setInhabitedTime(p_177415_1_);
    }

    @Override
    public long getInhabitedTime() {
        return delegate.getInhabitedTime();
    }

    @Override
    public boolean isLightCorrect() {
        return delegate.isLightCorrect();
    }

    @Override
    public void setLightCorrect(boolean p_217305_1_) {
        delegate.setLightCorrect(p_217305_1_);
    }

    @Override
    @Nullable
    public IWorld getWorldForge() {
        return delegate.getWorldForge();
    }

    @Override
    @Nullable
    public TileEntity getBlockEntity(BlockPos p_175625_1_) {
        return delegate.getBlockEntity(p_175625_1_);
    }

    @Override
    public BlockState getBlockState(BlockPos p_180495_1_) {
        return delegate.getBlockState(p_180495_1_);
    }

    @Override
    public FluidState getFluidState(BlockPos p_204610_1_) {
        return delegate.getFluidState(p_204610_1_);
    }

    @Override
    public int getLightEmission(BlockPos p_217298_1_) {
        return delegate.getLightEmission(p_217298_1_);
    }

    @Override
    public int getMaxLightLevel() {
        return delegate.getMaxLightLevel();
    }

    @Override
    public int getMaxBuildHeight() {
        return delegate.getMaxBuildHeight();
    }

    @Override
    public Stream<BlockState> getBlockStates(AxisAlignedBB p_234853_1_) {
        return delegate.getBlockStates(p_234853_1_);
    }

    @Override
    public BlockRayTraceResult clip(RayTraceContext p_217299_1_) {
        return delegate.clip(p_217299_1_);
    }

    @Override
    @Nullable
    public BlockRayTraceResult clipWithInteractionOverride(Vector3d p_217296_1_, Vector3d p_217296_2_, BlockPos p_217296_3_, VoxelShape p_217296_4_, BlockState p_217296_5_) {
        return delegate.clipWithInteractionOverride(p_217296_1_, p_217296_2_, p_217296_3_, p_217296_4_, p_217296_5_);
    }

    @Override
    public double getBlockFloorHeight(VoxelShape p_242402_1_, Supplier<VoxelShape> p_242402_2_) {
        return delegate.getBlockFloorHeight(p_242402_1_, p_242402_2_);
    }

    @Override
    public double getBlockFloorHeight(BlockPos p_242403_1_) {
        return delegate.getBlockFloorHeight(p_242403_1_);
    }

    @Override
    @Nullable
    public StructureStart<?> getStartForFeature(Structure<?> p_230342_1_) {
        return delegate.getStartForFeature(p_230342_1_);
    }

    @Override
    public void setStartForFeature(Structure<?> p_230344_1_, StructureStart<?> p_230344_2_) {
        delegate.setStartForFeature(p_230344_1_, p_230344_2_);
    }

    @Override
    public LongSet getReferencesForFeature(Structure<?> p_230346_1_) {
        return delegate.getReferencesForFeature(p_230346_1_);
    }

    @Override
    public void addReferenceForFeature(Structure<?> p_230343_1_, long p_230343_2_) {
        delegate.addReferenceForFeature(p_230343_1_, p_230343_2_);
    }

    @Override
    public Map<Structure<?>, LongSet> getAllReferences() {
        return delegate.getAllReferences();
    }

    @Override
    public void setAllReferences(Map<Structure<?>, LongSet> p_201606_1_) {
        delegate.setAllReferences(p_201606_1_);
    }
}
